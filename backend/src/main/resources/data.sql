-- 【给前端同学】初始数据脚本，≈ 前端 src/mock/data.js 里那 10 条假数据的「真数据库版」。
-- 由 application.yml 的 spring.sql.init.data-locations 在建表之后自动执行，
-- 所以后端一启动，接口就能查到和 Mock 完全一样的 10 条数据，前端页面无需任何改动。
-- H2 和 MySQL 通用（同一个文件两个 profile 都能用）。

-- 刻意不写 id 列：交给数据库自增生成，避免手工指定的值和自增序列冲突。
-- 列顺序：name, age, class_name, phone —— 注意第三列是下划线 class_name（对应前端 className）。
INSERT INTO students (name, age, class_name, phone) VALUES ('张三', 20, '计科1班', '13800138000'); -- 第 1 条，id 自动为 1
INSERT INTO students (name, age, class_name, phone) VALUES ('李四', 21, '计科2班', '13900139000'); -- 第 2 条，id 自动为 2
INSERT INTO students (name, age, class_name, phone) VALUES ('王五', 19, '计科1班', '13700137000'); -- 第 3 条
INSERT INTO students (name, age, class_name, phone) VALUES ('赵六', 22, '软工1班', '13600136000'); -- 第 4 条
INSERT INTO students (name, age, class_name, phone) VALUES ('孙七', 20, '软工2班', '13500135000'); -- 第 5 条
INSERT INTO students (name, age, class_name, phone) VALUES ('周八', 21, '计科3班', '13400134000'); -- 第 6 条
INSERT INTO students (name, age, class_name, phone) VALUES ('吴九', 19, '软工1班', '13300133000'); -- 第 7 条
INSERT INTO students (name, age, class_name, phone) VALUES ('郑十', 22, '计科2班', '13200132000'); -- 第 8 条
INSERT INTO students (name, age, class_name, phone) VALUES ('刘一', 20, '软工2班', '13100131000'); -- 第 9 条
INSERT INTO students (name, age, class_name, phone) VALUES ('陈二', 21, '计科3班', '13000130000'); -- 第 10 条
-- 合计 10 条：页面上默认显示 10 条、搜索「张三」只命中 1 条，都依赖这份数据。
-- H2 是内存库且 mode: always，所以每次重启都会重新执行这 10 行 → 你在页面上做的增删改会被「重置」。
