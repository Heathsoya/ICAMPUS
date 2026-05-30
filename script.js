const baseUrl = "";

const tabs = document.querySelectorAll(".tab");
const panels = document.querySelectorAll(".panel");
const csvInput = document.getElementById("csvInput");
const fileName = document.getElementById("fileName");

tabs.forEach((tab) => {
  tab.addEventListener("click", () => {
    tabs.forEach((item) => item.classList.remove("active"));
    panels.forEach((panel) => panel.classList.remove("active"));

    tab.classList.add("active");
    document.getElementById(tab.dataset.target).classList.add("active");
  });
});

async function requestJson(path, options = {}) {
  const res = await fetch(baseUrl + path, options);
  const data = await res.json().catch(() => ({}));

  if (!res.ok || data.code !== 200) {
    throw new Error(data.message || "请求失败");
  }

  return data.data;
}

document.getElementById("loginBtn").addEventListener("click", async () => {
  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;

  if (username === "" || password === "") {
    alert("请输入账号密码");
    return;
  }

  try {
    const data = await requestJson("/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ username, password })
    });

    localStorage.setItem("token", data.token);
    alert("登录成功，已获取模拟 token");
  } catch (error) {
    alert(error.message);
  }
});

document.getElementById("registerBtn").addEventListener("click", async () => {
  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;

  if (username === "" || password === "") {
    alert("请输入账号密码");
    return;
  }

  try {
    await requestJson("/api/auth/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ username, password })
    });

    alert("注册成功");
  } catch (error) {
    alert(error.message);
  }
});

document.getElementById("askBtn").addEventListener("click", async () => {
  const question = document.getElementById("question").value.trim();
  const answer = document.getElementById("answer");

  if (question === "") {
    alert("问题不能为空");
    return;
  }

  answer.value = "正在请求后端接口...";

  try {
    const data = await requestJson("/api/qa/ask", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ question })
    });

    answer.value = data.answer;
  } catch (error) {
    answer.value = error.message;
  }
});

document.getElementById("loadBtn").addEventListener("click", async () => {
  try {
    const data = await requestJson("/api/questions");
    renderTable(data);
  } catch (error) {
    alert(error.message);
  }
});

document.getElementById("chooseBtn").addEventListener("click", () => {
  csvInput.click();
});

csvInput.addEventListener("change", () => {
  const file = csvInput.files[0];
  fileName.textContent = file ? `已选择：${file.name}` : "CSV格式：question,category,answer";
});

document.getElementById("uploadBtn").addEventListener("click", async () => {
  const file = csvInput.files[0];

  if (!file) {
    alert("请先选择CSV文件");
    return;
  }

  const formData = new FormData();
  formData.append("file", file);

  try {
    const data = await requestJson("/api/questions/upload", {
      method: "POST",
      body: formData
    });

    alert(`上传成功，新增 ${data.count} 条数据`);
    const list = await requestJson("/api/questions");
    renderTable(list);
  } catch (error) {
    alert(error.message);
  }
});

function renderTable(data) {
  const dataBody = document.getElementById("dataBody");

  dataBody.innerHTML = data
    .map((item) => {
      return `
        <tr>
          <td>${item.id}</td>
          <td>${item.question}</td>
          <td>${item.category}</td>
        </tr>
      `;
    })
    .join("");
}
