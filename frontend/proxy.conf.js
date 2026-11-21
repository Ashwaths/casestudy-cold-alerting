const target = process.env.API_PROXY_TARGET;

if (!target) {
  throw new Error('Set API_PROXY_TARGET to the backend URL before starting the Angular development server.');
}

module.exports = {
  '/api': {
    target,
    secure: true
  }
};
