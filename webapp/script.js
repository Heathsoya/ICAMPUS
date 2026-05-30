const baseUrl = "http://localhost:8080";

const tabs = document.querySelectorAll(".tab");
const panels = document.querySelectorAll(".panel");

tabs.forEach((tab) => {
  tab.addEventListener("click", () => {
    tabs.forEach((item) => item.classList.remove("active"));
    panels.forEach((panel) => panel.classList.remove("active"));

    tab.classList.add("active");
    document.getElementById(tab.dataset.target).classList.add("active");
  });
});

document.getElementById("loginBtn").addEventListener("click", async () => {
  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;

  if (username === "" || password === "") {
    alert("请输入账号密码");
    return;
  }

  try {
    await fetch(baseUrl + "/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        username,
        password
      })
    });

    alert("模拟登录成功");
  } catch (error) {
    alert("登录失败");
  }
});

document.getElementById("registerBtn").addEventListener("click", () => {
  alert("模拟注册成功");
});

document.getElementById("askBtn").addEventListener("click", () => {
  const question = document.getElementById("question").value.trim();
  const answer = document.getElementById("answer");

  if (question === "") {
    alert("问题不能为空");
    return;
  }

  if (question.includes("食堂")) {
    answer.value = "学校食堂一般晚上九点左右关门";
  } else if (question.includes("图书馆")) {
    answer.value = "图书馆一般不建议带饮料";
  } else if (question.includes("校园卡")) {
    answer.value = "校园卡丢失后需要及时挂失";
  } else {
    answer.value = "暂无相关回答";
  }
});

document.getElementById("loadBtn").addEventListener("click", () => {
  const data = [
    { id: "1", question: "食堂几点关门", category: "食堂" },
    { id: "2", question: "图书馆可以带饮料吗", category: "图书馆" },
    { id: "3", question: "校园卡丢了怎么办", category: "校园卡" }
  ];

  renderTable(data);
  alert("获取数据成功");
});

document.getElementById("chooseBtn").addEventListener("click", () => {
  document.getElementById("csvInput").click();
});

document.getElementById("uploadBtn").addEventListener("click", () => {
  alert("模拟上传成功");
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
