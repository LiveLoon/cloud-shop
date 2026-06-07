// 本地 SVG 占位图（Base64）
export const DEFAULT_PRODUCT_IMAGE = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="200" viewBox="0 0 200 200"%3E%3Crect width="200" height="200" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" font-size="16" fill="%23999" text-anchor="middle" dy=".3em"%3E暂无图片%3C/text%3E%3C/svg%3E'

export const getProductImageUrl = (imageUrl) => {
  if (!imageUrl) return DEFAULT_PRODUCT_IMAGE
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) return imageUrl
  const IMAGE_BASE_URL = import.meta.env.VITE_IMAGE_BASE_URL || ''
  return `${IMAGE_BASE_URL}${imageUrl}`
}