import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export const errors = new Rate('errors');

export const options = {
  scenarios: {
    get_products: {
      executor: 'ramping-arrival-rate',
      timeUnit: '1s',
      preAllocatedVUs: 400,
      maxVUs: 1200,
      stages: [
        { target: 40, duration: '1m'  },
        { target: 46, duration: '2m'  },
        { target: 460, duration: '2m'  },
        { target:   0, duration: '1m'  },
      ],
    },
  },
  thresholds: {
    'http_req_failed': ['rate<0.001'],
    'http_req_duration{ep:products_list}': ['p(95)<600'],
  },
};

const BASE = 'http://localhost:8080';

export default function () {
  const res = http.get(`${BASE}/api/v1/products`, {
    tags: { ep: 'products_list' },
  });

  check(res, { 'status 200': (r) => r.status === 200 }) || errors.add(1);

  sleep(Math.random() * 0.4 + 0.1);
}
