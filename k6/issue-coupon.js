import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export const errors = new Rate('errors');

export const options = {
  scenarios: {
    issue_coupon: {
      executor: 'ramping-arrival-rate',
      timeUnit: '1s',
      preAllocatedVUs: 400,
      maxVUs: 1200,
      stages: [
        { target: 10,  duration: '1m' },
        { target: 46,  duration: '2m' },
        { target: 120, duration: '2m' },
        { target: 0,   duration: '1m' },
      ],
    },
  },
  thresholds: {
    'http_req_failed': ['rate<0.2'],
    'http_req_duration{ep:coupon_issue}': ['p(95)<600'],
  },
};

const BASE = 'http://localhost:8080';
const USERS = 20000;
const COUPON_ID = 100;

export default function () {
  const userId = Math.floor(Math.random() * USERS) + 1;
  const url = `${BASE}/api/v1/users/${userId}/coupons/${COUPON_ID}/issue`;

  const res = http.put(url, null, { tags: { ep: 'coupon_issue' } });

  check(res, { 'status 200': (r) => r.status === 200 }) || errors.add(1);

  sleep(Math.random() * 0.4 + 0.1);
}
