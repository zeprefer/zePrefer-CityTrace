let commonURL = "/api";
// 设置后台服务地址
axios.defaults.baseURL = commonURL;
axios.defaults.timeout = 5000;
// request拦截器，将用户token放入头中
let token = sessionStorage.getItem("token");
axios.interceptors.request.use(
  config => {
    if(token) config.headers['authorization'] = token
    return config
  },
  error => {
    console.log(error)
    return Promise.reject(error)
  }
)
axios.interceptors.response.use(function (response) {
  // 判断执行结果
  if (!response.data.success) {
    return Promise.reject(response.data.errorMsg)
  }
  return response.data;
}, function (error) {
  // 一般是服务端异常或者网络异常
  console.log(error)
  if(error.response && error.response.status === 401){
    // 未登录，跳转
    setTimeout(() => {
      location.href = "/login.html"
    }, 200);
    return Promise.reject("请先登录");
  }
  if (error.code === "ECONNABORTED") {
    return Promise.reject("请求超时，请稍后重试");
  }
  return Promise.reject("网络异常，请检查服务是否正常运行");
});
axios.defaults.paramsSerializer = function(params) {
  return Object.keys(params)
    .filter(k => params[k] !== undefined && params[k] !== null && params[k] !== "")
    .map(k => encodeURIComponent(k) + "=" + encodeURIComponent(params[k]))
    .join("&");
}
const util = {
  commonURL,
  getUrlParam(name) {
    let reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    let r = window.location.search.substr(1).match(reg);
    if (r != null) {
      return decodeURI(r[2]);
    }
    return "";
  },
  formatPrice(val) {
    const amount = Number(val);
    if (!Number.isFinite(amount)) return null;
    return (amount / 100).toFixed(2);
  }
}
