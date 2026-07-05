// 后端接口基础地址
// 如果前后端部署在同一个服务器，可以保持为空
const baseUrl = "";

const tabs = document.querySelectorAll(".tab");
const tabLinks = document.querySelectorAll(".tab-link");
const panels = document.querySelectorAll(".panel");

let lastQuestion = "";
let lastAnswer = "";
let lastQuestionLogId = null;
let lastKnowledgeId = null;
let crawlerPollTimer = null;
let knowledgeCurrentPage = 1;
let knowledgeTotal = 0;
const knowledgePageSize = 50;

function switchPanel(targetId, activeTab) {
  if (targetId === "login-panel" && localStorage.getItem("token")) {
    alert("你已登录，可在个人中心查看账号信息或退出登录");
    targetId = "center-panel";
    activeTab = null;
  }

  if (targetId === "contribution-panel" && !localStorage.getItem("token")) {
    alert("请先登录后再提交贡献");
    targetId = "login-panel";
    activeTab = null;
  }

  if (targetId === "audit-panel") {
    const role = localStorage.getItem("role");
    if (role !== "ADMIN") {
      alert("只有管理员可以进入管理后台");
      return;
    }
  }

  tabs.forEach((item) => item.classList.remove("active"));
  panels.forEach((panel) => panel.classList.remove("active"));

  if (activeTab) {
    activeTab.classList.add("active");
  } else {
    const matchedTab = document.querySelector(`.tab[data-target="${targetId}"]`);
    if (matchedTab) matchedTab.classList.add("active");
  }

  document.getElementById(targetId).classList.add("active");
  refreshUserInfo();

  if (targetId === "audit-panel") {
    showAdminView("overview");
    loadAdminOverview();
  }
}

// 页面切换
tabs.forEach((tab) => {
  tab.addEventListener("click", () => {
    const targetId = tab.dataset.target;

    switchPanel(targetId, tab);
  });
});

tabLinks.forEach((btn) => {
  btn.addEventListener("click", () => {
    switchPanel(btn.dataset.target);
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
        password: password
      })
    });

    alert("注册成功，请登录");
    document.getElementById("username").value = "";
    document.getElementById("password").value = "";
  } catch (error) {
    alert(error.message);
  }
});

// 登录
document.getElementById("loginBtn").addEventListener("click", async () => {
  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;
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
        password: password
      })
    });

    const role = data.role || "USER";
    localStorage.setItem("token", data.token || "");
    localStorage.setItem("username", data.username || username);
    localStorage.setItem("role", role);

    alert("登录成功，当前身份：" + (role === "ADMIN" ? "管理员" : "普通用户"));
    document.getElementById("username").value = "";
    document.getElementById("password").value = "";
    refreshUserInfo();
    updateMenuByRole();
    switchPanel("home-panel");
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
  switchPanel("home-panel");
});

// 智能问答
document.getElementById("askBtn").addEventListener("click", async () => {
  const question = document.getElementById("question").value.trim();
  const answer = document.getElementById("answer");

  if (question === "") {
    alert("请输入问题");
    return;
  }

  answer.value = "查询中...";
  lastQuestion = question;
  lastAnswer = "";
  lastQuestionLogId = null;
  lastKnowledgeId = null;

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
    lastQuestionLogId = data.questionLogId || null;
    lastKnowledgeId = data.matchedKnowledgeId || null;
    answer.value = lastAnswer;
  } catch (error) {
    answer.value = error.message;
  }
});

// 示例问题
// 问答完成后不再把这里改成“相关信息”，始终展示固定的常见问题。
document.querySelectorAll(".example-question").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.getElementById("question").value = btn.textContent;
  });
});

// 反馈
document.getElementById("goodBtn").addEventListener("click", () => {
  sendFeedback("USEFUL");
});

document.getElementById("badBtn").addEventListener("click", () => {
  sendFeedback("USELESS");
});

async function sendFeedback(type) {
  const token = localStorage.getItem("token");

  if (!token) {
    alert("请先登录后再反馈");
    return;
  }

  if (!lastQuestionLogId) {
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
        questionLogId: lastQuestionLogId,
        knowledgeId: lastKnowledgeId,
        feedbackType: type,
        feedbackContent: ""
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
  const hotCount = document.getElementById("hotCount");
  if (hotCount) hotCount.textContent = list.length;

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
        answer: answer
      })
    });

    alert("提交成功，等待管理员审核");

    document.getElementById("conQuestion").value = "";
    document.getElementById("conAnswer").value = "";
  } catch (error) {
    alert(error.message);
  }
});

// 管理后台
document.getElementById("loadAuditBtn").addEventListener("click", loadAuditList);
document.getElementById("loadKnowledgeBtn").addEventListener("click", () => loadKnowledgeList(1));
document.getElementById("deleteSelectedKnowledgeBtn").addEventListener("click", deleteSelectedKnowledge);
document.getElementById("prevKnowledgePageBtn").addEventListener("click", () => loadKnowledgeList(knowledgeCurrentPage - 1));
document.getElementById("nextKnowledgePageBtn").addEventListener("click", () => loadKnowledgeList(knowledgeCurrentPage + 1));
document.getElementById("selectAllKnowledge").addEventListener("change", (event) => {
  document.querySelectorAll(".knowledge-checkbox").forEach((checkbox) => {
    checkbox.checked = event.target.checked;
  });
});

document.querySelectorAll(".admin-menu").forEach((button) => {
  button.addEventListener("click", () => {
    const view = button.dataset.adminView;
    showAdminView(view);
    if (view === "overview") loadAdminOverview();
    if (view === "audit") loadAuditList();
    if (view === "knowledge") loadKnowledgeList();
    if (view === "crawler") loadCrawlerStatus();
  });
});

function showAdminView(view) {
  if (view !== "crawler" && crawlerPollTimer) {
    clearTimeout(crawlerPollTimer);
    crawlerPollTimer = null;
  }
  document.querySelectorAll(".admin-menu").forEach((button) => {
    button.classList.toggle("active", button.dataset.adminView === view);
  });
  document.querySelectorAll(".admin-view").forEach((panel) => panel.classList.remove("active"));
  const target = document.getElementById(`admin-${view}-view`);
  if (target) target.classList.add("active");
}

async function loadAdminOverview() {
  if (localStorage.getItem("role") !== "ADMIN") return;

  try {
    const [auditData, knowledgeData] = await Promise.all([
      requestJson("/api/admin/audit"),
      requestJson("/api/admin/knowledge?limit=100")
    ]);
    const auditList = Array.isArray(auditData) ? auditData : auditData.list || [];
    const pendingItems = auditList.filter((item) => item.status === "pending");
    document.getElementById("pendingCount").textContent = pendingItems.length;
    document.getElementById("knowledgeCount").textContent = knowledgeData.total ?? 0;
    document.getElementById("crawlerCount").textContent = knowledgeData.crawlerCount ?? 0;
  } catch (error) {
    alert(error.message);
  }
}

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
  const pendingCount = document.getElementById("pendingCount");
  const pendingItems = list.filter((item) => item.status === "pending");
  if (pendingCount) pendingCount.textContent = pendingItems.length;

  if (list.length === 0) {
    auditBody.innerHTML = "<tr><td colspan='6'>暂无待审核内容</td></tr>";
    return;
  }

  auditBody.innerHTML = list.map((item) => {
    const isPending = item.status === "pending";
    return `
      <tr>
        <td>${escapeHtml(item.id)}</td>
        <td>${escapeHtml(item.question || item.title)}</td>
        <td class="audit-answer">${escapeHtml(item.answer || "-")}</td>
        <td>${escapeHtml(item.submitter || "匿名用户")}</td>
        <td>${escapeHtml(formatAuditStatus(item.status))}</td>
        <td>
          ${isPending ? `
            <button onclick="auditItem(${Number(item.id)}, 'approved')">通过</button>
            <button class="danger-btn" onclick="auditItem(${Number(item.id)}, 'rejected')">驳回</button>
          ` : "已处理"}
        </td>
      </tr>
    `;
  }).join("");
}

async function loadKnowledgeList(page = knowledgeCurrentPage) {
  if (localStorage.getItem("role") !== "ADMIN") {
    alert("只有管理员可以查看知识库");
    return;
  }

  knowledgeCurrentPage = Math.max(1, Number(page) || 1);

  try {
    const data = await requestJson(`/api/admin/knowledge?page=${knowledgeCurrentPage}&limit=${knowledgePageSize}`);
    renderKnowledgeList(data);
  } catch (error) {
    alert(error.message);
  }
}

function renderKnowledgeList(data) {
  const summary = data || {};
  const list = Array.isArray(summary.items) ? summary.items : Array.isArray(summary.list) ? summary.list : [];
  knowledgeTotal = Number(summary.total ?? list.length);
  const totalPages = Math.max(1, Math.ceil(knowledgeTotal / knowledgePageSize));

  if (knowledgeCurrentPage > totalPages) {
    knowledgeCurrentPage = totalPages;
  }

  document.getElementById("knowledgeCount").textContent = knowledgeTotal;
  document.getElementById("crawlerCount").textContent = summary.crawlerCount ?? 0;
  updateKnowledgePagination(totalPages);

  const selectAll = document.getElementById("selectAllKnowledge");
  if (selectAll) selectAll.checked = false;

  const knowledgeBody = document.getElementById("knowledgeBody");
  if (list.length === 0) {
    knowledgeBody.innerHTML = "<tr><td colspan='8'>暂无知识库内容</td></tr>";
    return;
  }

  knowledgeBody.innerHTML = list.map((item) => {
    const isCrawler = (item.source || "").includes("爬虫");
    const updatedAt = (item.updatedAt || "").replace("T", " ").slice(0, 19);
    const itemId = Number(item.id);
    return `
      <tr>
        <td class="checkbox-cell"><input class="knowledge-checkbox" type="checkbox" value="${escapeHtml(item.id)}" aria-label="选择知识库记录 ${escapeHtml(item.id)}"></td>
        <td>${escapeHtml(item.id)}</td>
        <td>${escapeHtml(item.question)}</td>
        <td>${escapeHtml(item.category || "未分类")}</td>
        <td>${escapeHtml(item.keywords || "-")}</td>
        <td><span class="source-badge ${isCrawler ? "crawler" : ""}">${escapeHtml(item.source || "人工维护")}</span></td>
        <td>${escapeHtml(updatedAt || "-")}</td>
        <td><button class="danger-btn" onclick="deleteKnowledgeItem(${itemId})">删除</button></td>
      </tr>
    `;
  }).join("");
}

function updateKnowledgePagination(totalPages) {
  const pageInfo = document.getElementById("knowledgePageInfo");
  const prevButton = document.getElementById("prevKnowledgePageBtn");
  const nextButton = document.getElementById("nextKnowledgePageBtn");

  if (pageInfo) pageInfo.textContent = `第 ${knowledgeCurrentPage} 页 / 共 ${totalPages} 页，每页 ${knowledgePageSize} 条`;
  if (prevButton) prevButton.disabled = knowledgeCurrentPage <= 1;
  if (nextButton) nextButton.disabled = knowledgeCurrentPage >= totalPages;
}

function getSelectedKnowledgeIds() {
  return Array.from(document.querySelectorAll(".knowledge-checkbox:checked"))
    .map((checkbox) => Number(checkbox.value))
    .filter((id) => Number.isFinite(id));
}

async function deleteSelectedKnowledge() {
  const role = localStorage.getItem("role");
  if (role !== "ADMIN") {
    alert("只有管理员可以删除知识库记录");
    return;
  }

  const ids = getSelectedKnowledgeIds();
  if (ids.length === 0) {
    alert("请先选择要删除的知识库记录");
    return;
  }

  if (!confirm(`确认删除选中的 ${ids.length} 条知识库记录吗？`)) {
    return;
  }

  try {
    try {
      await requestJson("/api/admin/knowledge/batch", {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ids })
      });
    } catch (batchError) {
      await Promise.all(ids.map((id) => requestJson(`/api/admin/knowledge/${id}`, { method: "DELETE" })));
    }

    alert("删除成功");
    loadKnowledgeList(knowledgeCurrentPage);
  } catch (error) {
    alert(error.message);
  }
}

async function deleteKnowledgeItem(id) {
  const role = localStorage.getItem("role");
  if (role !== "ADMIN") {
    alert("只有管理员可以删除知识库条目");
    return;
  }

  const knowledgeId = Number(id);
  if (!Number.isFinite(knowledgeId) || knowledgeId <= 0) {
    alert("知识库条目 ID 无效");
    return;
  }

  if (!confirm("确定要删除该知识库条目吗？删除后不可恢复。")) {
    return;
  }

  try {
    await requestJson(`/api/admin/knowledge/${knowledgeId}`, { method: "DELETE" });
    alert("删除成功");
    loadKnowledgeList();
    loadAdminOverview();
  } catch (error) {
    alert(error.message);
  }
}

function formatAuditStatus(status) {
  if (status === "approved") return "已通过";
  if (status === "rejected") return "已驳回";
  return "待审核";
}

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, (character) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    "\"": "&quot;",
    "'": "&#39;"
  })[character]);
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
        status: result,
        reason: result === "approved" ? "内容审核通过" : "内容审核驳回"
      })
    });

    alert("审核操作成功");
    loadAuditList();
    loadKnowledgeList();
  } catch (error) {
    alert(error.message);
  }
}

// 爬虫任务管理
document.getElementById("refreshCrawlerBtn").addEventListener("click", () => loadCrawlerStatus());
document.getElementById("saveCrawlerScheduleBtn").addEventListener("click", saveCrawlerSchedule);
document.getElementById("runCrawlerBtn").addEventListener("click", runCrawlerNow);

async function loadCrawlerStatus(silent = false) {
  if (localStorage.getItem("role") !== "ADMIN") return;
  try {
    const data = await requestJson("/api/admin/crawler");
    renderCrawlerStatus(data);
  } catch (error) {
    if (!silent) alert(error.message);
  }
}

async function saveCrawlerSchedule() {
  const enabled = document.getElementById("crawlerEnabled").checked;
  const intervalHours = Number(document.getElementById("crawlerInterval").value);
  if (!Number.isInteger(intervalHours) || intervalHours < 1 || intervalHours > 168) {
    alert("执行周期必须是1到168之间的整数");
    return;
  }

  try {
    const data = await requestJson("/api/admin/crawler/schedule", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ enabled, intervalHours })
    });
    renderCrawlerStatus(data);
    alert(data.message || "爬虫周期已保存");
  } catch (error) {
    alert(error.message);
  }
}

async function runCrawlerNow() {
  const button = document.getElementById("runCrawlerBtn");
  button.disabled = true;
  try {
    const data = await requestJson("/api/admin/crawler/run", { method: "POST" });
    renderCrawlerStatus(data);
    alert(data.message || "爬虫任务已提交");
  } catch (error) {
    alert(error.message);
  } finally {
    if (!button.dataset.running) button.disabled = false;
  }
}

function renderCrawlerStatus(data) {
  const status = data || {};
  document.getElementById("crawlerAvailableState").textContent = status.available ? "可用" : "不可用";
  document.getElementById("crawlerRunningState").textContent = status.running ? "运行中" : "空闲";
  document.getElementById("crawlerIntervalState").textContent = `${status.intervalHours || 24} 小时`;
  document.getElementById("crawlerEnabled").checked = Boolean(status.enabled);
  document.getElementById("crawlerInterval").value = status.intervalHours || 24;
  document.getElementById("crawlerNextRun").textContent = formatCrawlerTime(status.nextRunAt);
  document.getElementById("crawlerLastStarted").textContent = formatCrawlerTime(status.lastStartedAt);
  document.getElementById("crawlerLastFinished").textContent = formatCrawlerTime(status.lastFinishedAt);
  document.getElementById("crawlerLastResult").textContent = status.lastExitCode == null
    ? "暂无记录"
    : status.lastExitCode === 0 ? "执行成功" : `执行失败（${status.lastExitCode}）`;

  const runButton = document.getElementById("runCrawlerBtn");
  runButton.disabled = !status.available || status.running;
  runButton.dataset.running = status.running ? "true" : "";

  if (crawlerPollTimer) clearTimeout(crawlerPollTimer);
  crawlerPollTimer = status.running
    ? setTimeout(() => loadCrawlerStatus(true), 5000)
    : null;
}

function formatCrawlerTime(value) {
  return value ? value.replace("T", " ").slice(0, 19) : "--";
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
  const token = localStorage.getItem("token");
  const adminTab = document.querySelector(".admin-tab");
  const loginTab = document.querySelector(".login-tab");
  const guestOnlyElements = document.querySelectorAll(".guest-only");
  const logoutButton = document.getElementById("logoutBtn");

  if (role === "ADMIN") {
    adminTab.style.display = "block";
  } else {
    adminTab.style.display = "none";
  }

  if (loginTab) loginTab.style.display = token ? "none" : "block";
  guestOnlyElements.forEach((element) => {
    element.style.display = token ? "none" : "inline-flex";
  });
  if (logoutButton) logoutButton.style.display = token ? "inline-flex" : "none";
}

refreshUserInfo();
updateMenuByRole();
