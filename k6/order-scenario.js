import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export const errors = new Rate('errors');

export const options = {
  scenarios: {
    order_journey_local: {
      executor: 'ramping-arrival-rate',
      timeUnit: '1s',
      preAllocatedVUs: 80,
      maxVUs: 200,
      stages: [
        { target: 15,  duration: '30s' },
        { target: 50,  duration: '60s' },
        { target: 120, duration: '60s' },
        { target: 0,   duration: '30s' },
      ],
    },
  },
  thresholds: {
    'http_req_failed': ['rate<0.02'],
    'http_req_duration{endpoint:product_detail}': ['p(95)<600'],
    'http_req_duration{endpoint:user_coupons}':   ['p(95)<600'],
    'http_req_duration{endpoint:order_place}':    ['p(95)<800'],
  },
  discardResponseBodies: true,
};

const BASE = __ENV.BASE || 'http://localhost:8080';
const USERS = Number(__ENV.USERS || 3000);
const PRODUCTS = Number(__ENV.PRODUCTS || 1000);

export default function () {
  const userId = rand(1, USERS);
  const productId = rand(1, PRODUCTS);

  const unitPrice = getProductDetail(productId);
  const { couponId, couponDiscount } = getUserCoupons(userId);
  createOrder(userId, productId, unitPrice, couponId, couponDiscount);

  sleep(Math.random() * 0.2 + 0.05);
}

function getProductDetail(productId) {
  let unitPrice = 0;
  const res = http.get(`${BASE}/api/v1/products/${productId}`, { tags: { endpoint: 'product_detail' }});
  check(res, { 'detail 2xx': r => r.status >= 200 && r.status < 300 }) || errors.add(1);
  try { unitPrice = Number(res.json()?.price ?? 0); } catch (_) {}
  return unitPrice;
}

function getUserCoupons(userId) {
  const res = http.get(`${BASE}/api/v1/users/${userId}/coupons`, { tags: { endpoint: 'user_coupons' }});
  check(res, { 'coupons 2xx': r => r.status >= 200 && r.status < 300 }) || errors.add(1);
  let couponId = null, couponDiscount = 0;
  try {
    const list = res.json();
    if (Array.isArray(list) && list.length > 0) {
      couponId = list[0]?.userCouponId ?? list[0]?.id ?? null;
      couponDiscount = Number(list[0]?.discountAmount ?? 0);
    }
  } catch (_) {}
  return { couponId, couponDiscount };
}

function createOrder(userId, productId, unitPrice, couponId, couponDiscount) {
  const quantity = 1;
  const orderAmount = unitPrice * quantity;
  const discountAmount = couponId ? couponDiscount : 0;
  const paymentAmount = Math.max(orderAmount - discountAmount, 0);
  const body = JSON.stringify({
    payment: { orderAmount, discountAmount, paymentAmount },
    products: [{ productId, quantity }],
    coupons: couponId ? [{ userCouponId: couponId, discountAmount }] : [],
  });
  const res = http.put(`${BASE}/api/v1/users/${userId}/orders`, body, {
    headers: { 'Content-Type': 'application/json' },
    tags: { endpoint: 'order_place' }
  });
  check(res, {
    'order 2xx': r => r.status >= 200 && r.status < 300,
    'order body shape': r => { try { return !!(r.json()?.id); } catch { return true; } },
  }) || errors.add(1);
}

function rand(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min; }
