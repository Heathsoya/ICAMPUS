// 后端接口基础地址
// 如果前后端部署在同一个服务器，可以保持为空
const baseUrl = "";

const tabs = document.querySelectorAll(".tab");
const panels = document.querySelectorAll(".panel");

let lastQuestion = "";
let lastAnswer = "";

// 页面切换
tabs.forEach((tab) => {
  tab.addEventListener("click", () => {
    const targetId = tab.dataset.target;

    if (targetId === "audit-panel") {
      const role = localStorage.getItem("role");
      if (role !== "ADMIN") {
        alert("只有管理员可以进入后台审核页面");
        return;
      }
    }

    tabs.forEach((item) => item.classList.remove("active"));
    panels.forEach((panel) => panel.classList.remove("active"));

    tab.classList.add("active");
    document.getElementById(targetId).classList.add("active");

    refreshUserInfo();
  });
});

// 统一请求函数
async function requestJson(path, options = {}) {
  const token = localStorage.getItem("token");

  if (!options.headers) {
    options.headers = {};
  }

  if (token) {
    options.headers.Authorization = "Bearer " + token;
  }

  const res = await fetch(baseUrl + path, options);
  const data = await res.json().catch(() => ({}));

  // 兼容两种常见返回格式：
  // 1. { code: 200, message: "成功", data: ... }
  // 2. { success: true, data: ... }
  if (!res.ok) {
    throw new Error(data.message || "请求失败");
  }

  if (data.code && data.code !== 200) {
    throw new Error(data.message || "请求失败");
  }

  if (data.success === false) {
    throw new Error(data.message || "请求失败");
  }

  return data.data || data;
}

// 注册
document.getElementById("registerBtn").addEventListener("click", async () => {
  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;
  const role = document.querySelector("input[name='role']:checked").value;

  if (username === "" || password === "") {
    alert("请输入账号和密码");
    return;
  }

  try {
    await requestJson("/api/auth/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        username: username,
        password: password,
        role: role
      })
    });

    alert("注册成功，请登录");
  } catch (error) {
    alert(error.message);
  }
});

// 登录
document.getElementById("loginBtn").addEventListener("click", async () => {
  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;
  const role = document.querySelector("input[name='role']:checked").value;

  if (username === "" || password === "") {
    alert("请输入账号和密码");
    return;
  }

  try {
    const data = await requestJson("/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        username: username,
        password: password,
        role: role
      })
    });

    localStorage.setItem("token", data.token || "");
    localStorage.setItem("username", username);
    localStorage.setItem("role", role);

    alert("登录成功，当前身份：" + (role === "ADMIN" ? "管理员" : "普通用户"));
    refreshUserInfo();
    updateMenuByRole();
  } catch (error) {
    alert(error.message);
  }
});

// 退出登录
document.getElementById("logoutBtn").addEventListener("click", () => {
  localStorage.removeItem("token");
  localStorage.removeItem("username");
  localStorage.removeItem("role");

  alert("已退出登录");
  refreshUserInfo();
  updateMenuByRole();
});

// 智能问答
document.getElementById("askBtn").addEventListener("click", async () => {
  const question = document.getElementById("question").value.trim();
  const answer = document.getElementById("answer");

  if (question === "") {
    alert("请输入问题");
    return;
  }

  answer.value = "正在请求后端接口，请稍等...";
  lastQuestion = question;

  try {
    const data = await requestJson("/api/qna/ask", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        question: question
      })
    });

    lastAnswer = data.answer || data.result || "暂无答案";
    answer.value = lastAnswer;
  } catch (error) {
    answer.value = error.message;
  }
});

// 示例问题
document.querySelectorAll(".example-question").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.getElementById("question").value = btn.textContent;
  });
});

// 反馈
document.getElementById("goodBtn").addEventListener("click", () => {
  sendFeedback("GOOD");
});

document.getElementById("badBtn").addEventListener("click", () => {
  sendFeedback("BAD");
});

async function sendFeedback(type) {
  const token = localStorage.getItem("token");

  if (!token) {
    alert("请先登录后再反馈");
    return;
  }

  if (lastQuestion === "") {
    alert("请先提问，再进行反馈");
    return;
  }

  try {
    await requestJson("/api/qna/feedback", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        question: lastQuestion,
        answer: lastAnswer,
        feedback: type
      })
    });

    alert("反馈成功");
  } catch (error) {
    alert(error.message);
  }
}

// 热点榜
document.getElementById("loadHotBtn").addEventListener("click", loadHotList);

async function loadHotList() {
  try {
    const data = await requestJson("/api/qna/hot");
    const list = Array.isArray(data) ? data : data.list || [];
    renderHotList(list);
  } catch (error) {
    alert(error.message);
  }
}

function renderHotList(list) {
  const hotBody = document.getElementById("hotBody");

  if (list.length === 0) {
    hotBody.innerHTML = "<tr><td colspan='3'>暂无热点数据</td></tr>";
    return;
  }

  hotBody.innerHTML = list.map((item, index) => {
    return `
      <tr>
        <td>${index + 1}</td>
        <td>${item.question || item.title || ""}</td>
        <td>${item.count || item.hot || item.num || 0}</td>
      </tr>
    `;
  }).join("");
}

// 用户贡献
document.getElementById("submitConBtn").addEventListener("click", async () => {
  const token = localStorage.getItem("token");

  if (!token) {
    alert("请先登录后再提交贡献");
    return;
  }

  const question = document.getElementById("conQuestion").value.trim();
  const answer = document.getElementById("conAnswer").value.trim();
  const category = document.getElementById("conCategory").value.trim();

  if (question === "" || answer === "") {
    alert("问题和答案不能为空");
    return;
  }

  try {
    await requestJson("/api/contribution", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        question: question,
        answer: answer,
        category: category
      })
    });

    alert("提交成功，等待管理员审核");

    document.getElementById("conQuestion").value = "";
    document.getElementById("conAnswer").value = "";
    document.getElementById("conCategory").value = "";
  } catch (error) {
    alert(error.message);
  }
});

// 后台审核列表
document.getElementById("loadAuditBtn").addEventListener("click", loadAuditList);

async function loadAuditList() {
  const role = localStorage.getItem("role");

  if (role !== "ADMIN") {
    alert("只有管理员可以加载审核列表");
    return;
  }

  try {
    const data = await requestJson("/api/admin/audit");
    const list = Array.isArray(data) ? data : data.list || [];
    renderAuditList(list);
  } catch (error) {
    alert(error.message);
  }
}

function renderAuditList(list) {
  const auditBody = document.getElementById("auditBody");

  if (list.length === 0) {
    auditBody.innerHTML = "<tr><td colspan='5'>暂无待审核内容</td></tr>";
    return;
  }

  auditBody.innerHTML = list.map((item) => {
    return `
      <tr>
        <td>${item.id}</td>
        <td>${item.question || item.title || ""}</td>
        <td>${item.username || item.user || ""}</td>
        <td>${item.status || "待审核"}</td>
        <td>
          <button onclick="auditItem(${item.id}, 'PASS')">通过</button>
          <button class="danger-btn" onclick="auditItem(${item.id}, 'REJECT')">驳回</button>
        </td>
      </tr>
    `;
  }).join("");
}

// 执行审核
async function auditItem(id, result) {
  const role = localStorage.getItem("role");

  if (role !== "ADMIN") {
    alert("只有管理员可以审核");
    return;
  }

  try {
    await requestJson("/api/admin/audit", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        id: id,
        result: result
      })
    });

    alert("审核操作成功");
    loadAuditList();
  } catch (error) {
    alert(error.message);
  }
}

// 个人中心信息
function refreshUserInfo() {
  const username = localStorage.getItem("username");
  const role = localStorage.getItem("role");
  const token = localStorage.getItem("token");

  document.getElementById("currentUser").textContent = username || "未登录";
  document.getElementById("currentRole").textContent =
    role === "ADMIN" ? "管理员" : role === "USER" ? "普通用户" : "游客";
  document.getElementById("tokenState").textContent = token ? "已获取 Token" : "无 Token";
}

// 根据身份控制菜单显示
function updateMenuByRole() {
  const role = localStorage.getItem("role");
  const adminTab = document.querySelector(".admin-tab");

  if (role === "ADMIN") {
    adminTab.style.display = "block";
  } else {
    adminTab.style.display = "none";
  }
}

refreshUserInfo();
updateMenuByRole();
