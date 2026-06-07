// utils/imageHelper.js
// 默认占位图（可使用本地图片或网络图片）
export const DEFAULT_PRODUCT_IMAGE = 'https://via.placeholder.com/200x200?text=No+Image';

// 根据后端返回的 imageUrl 获取完整可访问的图片地址
// 若后端返回的是相对路径，需拼接基础 URL（例如静态资源服务器地址）
export const getProductImageUrl = (imageUrl) => {
  if (!imageUrl) return DEFAULT_PRODUCT_IMAGE;

  // 如果已经是完整 URL，直接返回
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl;
  }

  // 若是相对路径，拼接你的静态资源基础 URL（根据实际情况修改）
  const IMAGE_BASE_URL = import.meta.env.VITE_IMAGE_BASE_URL || '';
  return `${IMAGE_BASE_URL}${imageUrl}`;
};