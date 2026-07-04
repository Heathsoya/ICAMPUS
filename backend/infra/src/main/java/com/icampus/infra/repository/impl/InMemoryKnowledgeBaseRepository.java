package com.icampus.infra.repository.impl;

import com.icampus.domain.entity.KnowledgeBase;
import com.icampus.domain.repository.KnowledgeBaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 知识库仓储 — 内存实现
 * <p>
 * 开发阶段使用，后续替换为 MySQL + MyBatis-Plus 全文检索。
 */
public class InMemoryKnowledgeBaseRepository implements KnowledgeBaseRepository {

    private static final Logger log = LoggerFactory.getLogger(InMemoryKnowledgeBaseRepository.class);

    private final Map<Long, KnowledgeBase> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public InMemoryKnowledgeBaseRepository() {
        // 预置一些校园 FAQ 测试数据
        addMockData();
    }

    @Override
    public KnowledgeBase save(KnowledgeBase knowledgeBase) {
        KnowledgeBase existing = store.values().stream()
                .filter(item -> item.getQuestion().equals(knowledgeBase.getQuestion()))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            knowledgeBase.setId(existing.getId());
            knowledgeBase.setCreatedAt(existing.getCreatedAt());
        } else if (knowledgeBase.getId() == null) {
            knowledgeBase.setId(idGenerator.getAndIncrement());
        }
        if (knowledgeBase.getCreatedAt() == null) {
            knowledgeBase.setCreatedAt(java.time.LocalDateTime.now());
        }
        knowledgeBase.setUpdatedAt(java.time.LocalDateTime.now());
        store.put(knowledgeBase.getId(), knowledgeBase);
        return knowledgeBase;
    }

    @Override
    public List<KnowledgeBase> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>(store.values());
        }

        String[] terms = keyword.toLowerCase().split("\\s+");
        return store.values().stream()
                .filter(kb -> {
                    String text = (kb.getQuestion() + " " + kb.getAnswer() + " " + kb.getKeywords()).toLowerCase();
                    for (String term : terms) {
                        if (text.contains(term)) return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<KnowledgeBase> findByCategory(String category) {
        return store.values().stream()
                .filter(kb -> category.equals(kb.getCategory()))
                .collect(Collectors.toList());
    }

    @Override
    public List<KnowledgeBase> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public KnowledgeBase findById(Long id) {
        return store.get(id);
    }

    // ====== 预置测试数据 ======

    private void addMockData() {
        add("宿舍几点关门？", "学生宿舍每晚23:00关门，周末及节假日延长至23:30。晚归需在宿管处登记。",
                "住宿生活", "宿舍 关门 时间 门禁");
        add("校园卡丢失怎么补办？", "请携带身份证到学生服务中心一楼校园卡窗口办理挂失和补办，工本费20元。也可先在「校园卡自助服务平台」挂失。",
                "校园服务", "校园卡 丢失 补办 挂失");
        add("图书馆借书期限是多久？", "本科生借书期限为30天，可续借1次（+30天）。研究生借书期限为60天，可续借1次（+30天）。逾期按0.1元/天罚款。",
                "图书馆", "图书馆 借书 期限 续借");
        add("奖学金申请条件是什么？", "校级奖学金要求学年成绩排名前30%，无挂科记录，无违纪处分。具体分为一等奖学金（前5%）、二等奖学金（前15%）、三等奖学金（前30%）。每年9月开放申请。",
                "财务缴费", "奖学金 申请 条件 成绩");
        add("如何选课？", "每学期第1-2周为选课周，通过教务系统（jwxt.campus.edu.cn）进行选课。必修课系统自动预置，选修课需自行抢选。第3周可进行退改选。",
                "教务教学", "选课 教务系统 选修 必修");
        add("食堂营业时间？", "早餐 6:30-8:30，午餐 11:00-13:00，晚餐 17:00-19:00。夜宵窗口开放至22:00（仅第一食堂）。",
                "餐饮服务", "食堂 营业时间 早餐 午餐 晚餐");
        add("毕业学分要求是多少？", "本科毕业要求修满160学分，其中必修课≥100学分，选修课≥30学分，实践环节≥20学分，毕业论文10学分。",
                "就业毕业", "毕业 学分 要求 必修 选修");
        add("校园网怎么连？", "连接WiFi「Campus-WiFi」，使用学号和统一认证密码登录。每个账号最多同时连接2台设备。如遇网络故障请联系信息中心：027-12345678。",
                "校园服务", "校园网 WiFi 网络 上网");
    }

    private void add(String question, String answer, String category, String keywords) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId(idGenerator.getAndIncrement());
        kb.setQuestion(question);
        kb.setAnswer(answer);
        kb.setCategory(category);
        kb.setKeywords(keywords);
        kb.setCreatedAt(java.time.LocalDateTime.now());
        kb.setUpdatedAt(java.time.LocalDateTime.now());
        store.put(kb.getId(), kb);
    }
}
