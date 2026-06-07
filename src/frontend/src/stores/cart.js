import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: []
  }),
  
  getters: {
    totalCount: (state) => state.items.reduce((sum, item) => sum + item.buyNum, 0),
    totalPrice: (state) => state.items.reduce((sum, item) => sum + (item.price * item.buyNum), 0),
    cartItems: (state) => state.items
  },
  
  actions: {
    loadCart() {
      const stored = localStorage.getItem('cart')
      if (stored) {
        this.items = JSON.parse(stored)
      }
    },
    
    saveCart() {
      localStorage.setItem('cart', JSON.stringify(this.items))
    },
    
    addItem(product) {
      const existing = this.items.find(item => item.productId === product.id)
      if (existing) {
        existing.buyNum++
      } else {
        this.items.push({
          productId: product.id,
          productName: product.productName,
          price: product.price,
          buyNum: 1
        })
      }
      this.saveCart()
    },
    
    removeItem(productId) {
      this.items = this.items.filter(item => item.productId !== productId)
      this.saveCart()
    },
    
    updateQuantity(productId, buyNum) {
      const item = this.items.find(item => item.productId === productId)
      if (item) {
        if (buyNum <= 0) {
          this.removeItem(productId)
        } else {
          item.buyNum = buyNum
          this.saveCart()
        }
      }
    },
    
    clearCart() {
      this.items = []
      this.saveCart()
    }
  }
})