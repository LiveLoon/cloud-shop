package edu.huhst.listener;

import edu.huhst.domain.Payment;
import edu.huhst.enums.PaymentStatus;
import edu.huhst.repository.PaymentRepository;
import edu.huhst.service.PaymentService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Numeric;

@Component
@EnableScheduling
public class BlockChainPaymentListener {
    private static final Logger log = LoggerFactory.getLogger(BlockChainPaymentListener.class);
    @Autowired
    private Web3j web3j;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PaymentService paymentService;


    @Value("${blockchain.platform-address}")
    private String platformAddress;

    @Value("${blockchain.required-confirmations}")
    private BigInteger requiredConfirmations;


    private BigInteger lastProcessedBlock = BigInteger.valueOf(-1);

    @PostConstruct
    public void init(){
        try {
            EthBlock.Block latestBlock = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST,false)
                    .send()
                    .getBlock();
            log.info("监听器启动，起始区块: {}", lastProcessedBlock);
            if (latestBlock != null) {
                // 从最新区块往前 10 块开始，避免处理全部历史
                lastProcessedBlock = latestBlock.getNumber().subtract(BigInteger.valueOf(10));
                log.info("监听器启动，起始区块: {}", lastProcessedBlock);
            }
        }catch (Exception e){
            log.error("获取最新区块失败", e);
        }
    }


    @Scheduled(fixedDelayString = "${blockchain.poll-interval-ms}")
    public void pollNewBlocks() {
        try {
            BigInteger currentBlock = web3j.ethBlockNumber().send().getBlockNumber();
            log.info("获得区块: {}",currentBlock);
            if (lastProcessedBlock.compareTo(currentBlock) >= 0) {
                return;
            }
            for (BigInteger i = lastProcessedBlock.add(BigInteger.ONE); i.compareTo(currentBlock) <= 0; i = i.add(BigInteger.ONE)) {
                processBlock(i);
            }
        } catch (Exception e) {
            log.error("轮询区块失败", e);
        }
    }

    private void processBlock(BigInteger blockNumber) throws Exception{

        EthBlock.Block block = web3j.ethGetBlockByNumber(org.web3j.protocol.core.DefaultBlockParameter.valueOf(blockNumber), true).send().getBlock();
        if (block == null) return;
        List<EthBlock.TransactionResult> txs = block.getTransactions();
        for (EthBlock.TransactionResult txResult: txs){
            Transaction tx =(Transaction) txResult.get();

            if (tx.getTo() == null || !tx.getTo().equalsIgnoreCase(platformAddress)) {
                continue;
            }


            // 解析 input data
            String inputData = tx.getInput();
            if (inputData == null || inputData.equals("0x")) {
                log.debug("交易 {} 没有附带data，忽略", tx.getHash());
                continue;
            }

            // 将十六进制 data 转为字符串
            String decoded;
            try {
                byte[] bytes = Numeric.hexStringToByteArray(inputData);
                decoded = new String(bytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.warn("解析data失败: {}", inputData);
                continue;
            }


            Integer orderId;
            try {
                orderId = Integer.parseInt(decoded);
            }catch (NumberFormatException e) {
                log.warn("无法解析订单号: {}", decoded);
                continue;
            }

            Optional<Payment> paymentOpt = paymentRepository.findByOrderId(orderId);
            if (paymentOpt.isEmpty()) {
                log.warn("未找到订单 {} 的支付记录", orderId);
                continue;
            }


            Payment payment = paymentOpt.get();
            if (!PaymentStatus.CREATED.equals(payment.getStatus())) {
                log.debug("订单 {} 已处理过，跳过", orderId);
                continue;
            }


            BigInteger txValue = tx.getValue();   // 单位 Wei
            if (txValue.compareTo(payment.getValue()) < 0) {
                log.warn("订单 {} 金额不足: 预期 {} wei, 收到 {} wei", orderId, payment.getValue(), txValue);
                paymentService.markAsFailed(payment.getId(), "支付金额不足");
                continue;
            }



// 确认数
            BigInteger currentBlockNum = web3j.ethBlockNumber().send().getBlockNumber();
            BigInteger confirmations = currentBlockNum.subtract(blockNumber).add(BigInteger.ONE);




            if (confirmations.compareTo(requiredConfirmations) >= 0) {
                paymentService.markAsPaid(tx.getHash(), payment.getId(), txValue, confirmations.intValue());
                log.info("订单 {} 支付成功，tx: {}, 确认数: {}", orderId, tx.getHash(), confirmations);
            } else {
                paymentRepository.save(payment);
                log.info("订单 {} 收到交易但确认数不足 {} / {}", orderId, confirmations, requiredConfirmations);
            }
        }

    }
}

