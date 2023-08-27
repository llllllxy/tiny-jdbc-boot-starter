<h1 align="center">tiny-jdbc-boot-starter</h1>

<p align="center">
	<a target="_blank" href="https://www.apache.org/licenses/LICENSE-2.0">
		<img src="https://img.shields.io/badge/license-Apache%202-green.svg" />
	</a>
	<a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
		<img src="https://img.shields.io/badge/JDK-8+-blue.svg" />
	</a>
    <a href='https://gitee.com/leisureLXY/tiny-security-boot-starter'>
        <img src='https://gitee.com/leisureLXY/tiny-jdbc-boot-starter/badge/star.svg?theme=dark' alt='star' />
    </a>
    <br/>
</p>

## 1、简介
`tiny-jdbc-boot-starter`是一个基于`Spring Data JDBC`开发的轻量级数据库ORM工具包，在不改变原有功能的基础上，做了大量的增强，让操作数据库这件事变得更加简单便捷！

### 优势
- **无侵入**：只做增强不做改变，引入它不会对现有工程产生任何影响
- **性能高**：基于高性能的Spring Data JDBC，性能基本无损耗
- **功能强**：既支持SQL操作、又支持实体类映射操作，BaseDao里封装了大量的公共方法，拿来即用，配合强大的条件构造器，基本满足各类使用需求
- **支持 Lambda 形式调用**：条件构造器支持Lambda形式调用，编译期增强，无需再担心字段写错
- **支持主键自动生成**：内含多种主键生成策略（包括自增主键、UUID、雪花ID）
- **支持多种数据库分页方言**：包括MySQL、ORACLE、DB2、PostgreSql等多种常用数据库

### 支持数据库
- 分页插件目前支持MySQL、ORACLE、DB2、PostgreSql、SQLITE、H2
- 其他操作支持任何使用标准 SQL 的数据库

## 2、快速开始
### 引入Maven依赖
```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.tinycloud</groupId>
        <artifactId>tiny-jdbc-boot-starter</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
```

### 定义Entity实体类，对应数据库的一张表
```java
@Table("b_upload_file")
public class UploadFile implements Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * 表的主键
     * 注意，如果设置为自增主键的话，则此字段必须为Long
     * 如果设置为uuid的话，则此字段必须为String
     * 如果设置为objectId的话，则此字段必须为String
     * 如果设置为assignId的话，则此字段必须为String或者Long
     */
    @Column(value = "id", primaryKey = true, assignId = true)
    private Long id;

    /**
     * 文件id
     */
    @Column("file_id")
    private String fileId;

    /**
     * 文件原名称
     */
    @Column("file_name_old")
    private String fileNameOld;

    /**
     * 文件新名称
     */
    @Column("file_name_new")
    private String fileNameNew;

    /**
     * 文件路径
     */
    @Column("file_path")
    private String filePath;

    /**
     * 文件md5
     */
    @Column("file_md5")
    private String fileMd5;

    /**
     * 上传时间
     */
    @Column("created_at")
    private Date createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileNameOld() {
        return fileNameOld;
    }

    public void setFileNameOld(String fileNameOld) {
        this.fileNameOld = fileNameOld;
    }

    public String getFileNameNew() {
        return fileNameNew;
    }

    public void setFileNameNew(String fileNameNew) {
        this.fileNameNew = fileNameNew;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
```

> 注解说明
> ##### @Table
> - 描述：表名注解，标识实体类对应的表
> - 使用位置：实体类
> ```java
> @Table("b_upload_file")
> public class UploadFile implements Serializable {
>     private static final long serialVersionUID = -1L;
>     
>     ...
> }
> ```
> ##### @Column
> - 描述：字段注解
> - 使用位置：实体类
> ```java
> @Table("b_upload_file")
> public class UploadFile implements Serializable {
>     private static final long serialVersionUID = -1L;
>
>     /**
>      * 表的主键，四种主键策略互斥，只能选择其中一种
>      * 注意，如果设置为数据库自增主键的话，则此字段必须为Long
>      * 如果设置为uuid的话，则此字段必须为String
>      * 如果设置为objectId的话，则此字段必须为String
>      * 如果设置为assignId的话，则此字段必须为String或者Long
>      */
>     @Column(value = "id", primaryKey = true, assignId = true)
>     private Long id;
>     
>     @Column("file_id")
>     private String fileId;
> 
>     @Column("file_name_old")
>    private String fileNameOld;
> }
> ```
> |属性|类型|必须指定|默认值|描述|
> |---|---|---|---|---|
> | value         | String  |  是 | ""    | 对应数据库字段名  |
> | primaryKey    | boolean |  否 | false | 是否为主键  |
> | autoIncrement | boolean |  否 | false | 主键策略：自增主键，四种主键策略互斥，只能选择其一 |
> | assignId      | boolean |  否 | false | 主键策略：雪花ID，四种主键策略互斥，只能选择其一  |
> | uuid          | boolean |  否 | false | 主键策略：UUID，四种主键策略互斥，只能选择其一 |
> | objectId      | boolean |  否 | false | 主键策略：MongoDB ObjectId，四种主键策略互斥，只能选择其一 |

### 定义Dao类，继承自BaseDao，泛型1为对应实体类，泛型二实体类主键类型
```java
    import org.springframework.stereotype.Repository;
    import org.tinycloud.jdbc.BaseDao;
    import org.tinycloud.entity.UploadFile;

    @Repository
    public class UploadFileDao extends BaseDao<UploadFile, Long> {
    }
```
### Service层注入即可使用
```java
    import org.springframework.stereotype.Repository;
    import org.tinycloud.jdbc.BaseDao;
    import org.tinycloud.entity.Project;

    @Repository
    public class UploadFileService extends BaseDao<Project> {
        
        @Autowired
        private ProjectDao projectDao;
        
    }
```


## 3、BaseDao接口说明

### 查询操作
|方法|说明|
|---|---|
|`<T> List<T> select(String sql, Class<T> classz, Object... params);` |根据给定的sql和实体类型和参数，查询数据库并返回实体类对象列表|
|`<T> T selectOne(String sql, Class<T> classz, Object... params);`|根据给定的sql和实体类型和参数，查询数据并返回一个实体类对象|
|`List<T> select(String sql, Object... params);` |根据给定的sql和参数，查询数据库并返回实体类对象列表，类型使用的是xxxDao<T>的类型|
|`T selectOne(String sql, Object... params);`|根据给定的sql和参数，查询数据并返回一个实体类对象，类型使用的是xxxDao<T>的类型|
|`List<Map<String, Object>> selectMap(String sql, Object... params);`|根据给定的sql和参数，查询数据库并返回Map<String, Object>列表|
|`Map<String, Object> selectOneMap(String sql, Object... params);`|根据给定的sql和参数，查询数据并返回一个Map<String, Object>对象|
|`<T> T selectOneColumn(String sql, Class<T> clazz, Object... params);`|根据给定的sql和实体类型和参数，查询数据并返回一个值（常用于查count）|
|`Page<T> paginate(String sql, Integer pageNumber, Integer pageSize);`|执行分页查询，返回Page对象，类型使用的是xxxDao<T>的类型|
|`Page<T> paginate(String sql, Integer pageNumber, Integer pageSize, Object... params);`|执行分页查询，返回Page对象，类型使用的是xxxDao<T>的类型|
|`T selectById(Object id);`|根据主键ID值，查询数据并返回一个实体类对象，类型使用的是xxxDao<T>的类型|
|`List<T> select(T entity);`|实体类里面非null的属性作为查询条件，查询数据库并返回实体类对象列表，类型使用的是xxxDao<T>的类型|
|`Page<T> paginate(T entity, Integer pageNumber, Integer pageSize);`|实体类里面非null的属性作为查询条件，执行分页查询，类型使用的是xxxDao<T>的类型|
|`T selectOne(T entity);`|实体类里面非null的属性作为查询条件，查询数据并返回一个实体类对象，类型使用的是xxxDao<T>的类型|
|`List<T> select(Criteria criteria);`|根据条件构造器查询，返回多条，类型使用的是xxxDao<T>的类型|
|`List<T> select(LambdaCriteria lambdaCriteria);`|根据条件构造器(lambda)查询，返回多条，查询数据并返回一个实体类对象，类型使用的是xxxDao<T>的类型|
|`T selectOne(Criteria criteria);`|根据条件构造器执行查询，返回一条，类型使用的是xxxDao<T>的类型|
|`T selectOne(LambdaCriteria lambdaCriteria);`|根据条件构造器(lambda)执行查询，返回一条，类型使用的是xxxDao<T>的类型|
|`Page<T> paginate(Criteria criteria, Integer pageNumber, Integer pageSize);`|根据条件构造器执行分页查询，返回Page对象，类型使用的是xxxDao<T>的类型|
|`Page<T> paginate(LambdaCriteria lambdaCriteria, Integer pageNumber, Integer pageSize, Object... params);`|根据条件构造器(lambda)执行分页查询，返回Page对象，类型使用的是xxxDao<T>的类型|

### 插入操作
|方法|说明|
|---|---|
|`int insert(String sql, final Object... params);`|根据提供的SQL语句和提供的参数，执行插入|
|`int insert(T entity);`|插入entity里的数据，将忽略entity里属性值为null的属性，如果主键策略为assignId、uuid或objectId，那将在entity里返回生成的主键值|
|`int insert(T entity, boolean ignoreNulls);`|插入entity里的数据，可选择是否忽略entity里属性值为null的属性，如果主键策略为assignId、uuid或objectId，那将在entity里返回生成的主键值|
|`Long insertReturnAutoIncrement(T entity);`|插入entity里的数据，将忽略entity里属性值为null的属性，并且返回自增的主键|

### 更新操作
|方法|说明|
|---|---|
|`int update(String sql, final Object... params);`|根据提供的SQL语句和提供的参数，执行修改|
|`int update(T entity, Criteria criteria);`|根据entity里的值和条件构造器，执行修改|
|`int update(T entity, LambdaCriteria criteria);`|根据entity里的值和条件构造器（lambda），执行修改|
|`int updateById(T entity);`|根据主键值作为条件更新数据，将忽略entity里属性值为null的属性|
|`int updateById(T entity, boolean ignoreNulls);`|根据主键值更新数据，可选择是否忽略entity里属性值为null的属性|

#### 删除操作
|方法|说明|
|---|---|
|`int delete(String sql, final Object... params);` | 根据提供的SQL语句和提供的参数，执行删除 |
|`int deleteById(Object id);` | 根据主键ID进行删除，类型使用的是xxxDao<T>的类型 |
|`int delete(T entity);`| 根据entity里的属性值进行删除，entity里不为null的属性，将作为参数 |
|`int delete(Criteria criteria);`| 根据条件构造器，将作为where参数 |
|`int delete(LambdaCriteria criteria);`| 根据条件构造器（lambda），将作为where参数 |

## 4、条件构造器

### Criteria示例
```java
    List<Integer> ids = new ArrayList<Integer>() {{
        add(1);
        add(2);
        add(3);
    }};

    Criteria criteria = new Criteria()
            .lt("age", 28)
            .eq("created_at", new java.util.Date())
            .in("id", ids)
            .orderBy("age", true);
    
    // 等价于  WHERE age < 28 AND created_at = '2023-08-05 17:31:26' AND id IN (1,2,3) ORDER BY age DESC
```

### LambdaCriteria示例
```java
public static void main(String[] args) {
    List<Long> ids = new ArrayList<Long>() {{
        add(1L);
        add(2L);
        add(3L);
    }};
    
    LambdaCriteria criteria = new LambdaCriteria()
            .lt(UploadFile::getFileId, "1000")
            .eq(UploadFile::getFileMd5, "b8394b15e02c50b508b3e46cc120f0f5")
            .in(UploadFile::getId, ids)
            .orderBy(UploadFile::getCreatedAt, true);

    // 等价于  WHERE fileId < '1000' AND file_md5 = 'b8394b15e02c50b508b3e46cc120f0f5' AND id IN (1,2,3) ORDER BY created_at DESC
```
### 说明

|方法|说明|示例|lambda实例|
|---|---|---|---|
|eq        | 等于 =      | eq("name", "张三") ---> AND name = '张三' | eq(User::getName, "张三") ---> AND name = '张三' |
|orEq      | 等于 =      | orEq("name", "张三") ---> OR name = '张三' | orEq(User::getName, "张三") ---> OR name = '张三' |
|notEq     | 不等于 <>   | notEq("name", "张三") ---> AND name <> '张三' | notEq(User::getName, "张三") ---> AND name <> '张三' |
|orNotEq   | 不等于 <>   | orNotEq("name", "张三") ---> OR name <> '张三' | orNotEq(User::getName, "张三") ---> OR name <> '张三' |
|isNull    | 等于null    | isNull("name") ---> AND name IS NULL | isNull(User::getName) ---> AND name IS NULL |
|orIsNull  | 等于null    | orIsNull("name") ---> OR name IS NULL | orIsNull(User::getName) ---> OR name IS NULL |
|isNotNull | 不等于null   | isNotNull("name") ---> AND name IS NOT NULL | isNotNull(User::getName) ---> AND name IS NOT NULL |
|orIsNotNull  | 不等于null    | orIsNotNull("name") ---> OR name IS NOT NULL | orIsNotNull(User::getName) ---> OR name IS NOT NULL |
|lt        | 小于 <      | lt("name", "张三") ---> AND name < '张三'  | lt(User::getName, "张三") ---> AND name < '张三' |
|orLt      | 小于 <      | orLt("name", "张三") ---> OR name < '张三'  | orLt(User::getName, "张三") ---> OR name < '张三' |
|lte       | 小于等于 <=  | lte("name", "张三") ---> AND name <= '张三' | lte(User::getName, "张三") ---> AND name <= '张三' |
|orLte     | 小于等于 <=  | orLte("name", "张三") ---> OR name <= '张三'  | orLte(User::getName, "张三") ---> OR name <= '张三' |
|gt        | 大于 >      | gt("name", "张三") ---> AND name > '张三'  | gt(User::getName, "张三") ---> AND name > '张三' |
|orGt      | 大于 >      | orGt("name", "张三") ---> OR name > '张三'  | orGt(User::getName, "张三") ---> OR name > '张三' |
|gte       | 大于等于 >=  | gte("name", "张三") ---> AND name >= '张三' | gte(User::getName, "张三") ---> AND name >= '张三' |
|orGte     | 大于等于 >=  | orGte("name", "张三") ---> OR name >= '张三'  | orGte(User::getName, "张三") ---> OR name >= '张三' |
|in        | SQL里的IN操作  | in("name", {"张三","李四"}) ---> AND name IN ('张三','李四')  | in(User::getName, "张三") ---> AND name IN ('张三','李四') |
|orIn      | SQL里的IN操作  | orIn("name", {"张三","李四"}) ---> OR name IN ('张三','李四')  | orIn(User::getName, "张三") ---> OR name IN ('张三','李四') |
|notIn     | SQL里的IN操作  | notIn("name", "张三") ---> AND name NOT IN ('张三','李四') | notIn(User::getName, "张三") ---> AND name NOT IN ('张三','李四') |
|orNotIn   | SQL里的IN操作  | orNotIn("name", {"张三","李四"}) ---> OR name NOT IN ('张三','李四')  | orNotIn(User::getName, "张三") ---> OR name NOT IN ('张三','李四') |
|between    | NOT BETWEEN 值1 AND 值2 | between("age", 10, 18) ---> AND (age BETWEEN 10 AND 18) | between(User::getAge, 10, 18) ---> AND (age BETWEEN 10 AND 18 ) |
|orBetween  | NOT BETWEEN 值1 AND 值2 | orBetween("age", 10, 18) ---> OR (age BETWEEN 10 AND 18) | orBetween(User::getAge, 10, 18) ---> OR (age BETWEEN 10 AND 18 ) |
|notBetween | NOT BETWEEN 值1 AND 值2 | notBetween("age", 10, 18) ---> AND (age NOT BETWEEN 10 AND 18) | notBetween(User::getAge, 10, 18) ---> AND (age NOT BETWEEN 10 AND 18 ) |
|orNotBetween| NOT BETWEEN 值1 AND 值2| orNotBetween("age", 10, 18) ---> OR (age NOT BETWEEN 10 AND 18) | orNotBetween(User::getAge, 10, 18) ---> OR (age NOT BETWEEN 10 AND 18 ) |
|like       | LIKE '%值%'     | like("name", "张三") ---> AND name LIKE '%张三%'  | like(User::getName, "张三") ---> AND name LIKE '%张三%' |
|orLike     | LIKE '%值%'     | orLike("name", "张三") ---> OR name LIKE '%张三%'  | orLike(User::getName, "张三") ---> OR name LIKE '%张三%' |
|notLike    | NOT LIKE '%值%' | notLike("name", "张三") ---> AND name NOT LIKE '%张三%'  | notLike(User::getName, "张三") ---> AND name NOT LIKE '%张三%' |
|orNotLike  | NOT LIKE '%值%' | orNotLike("name", "张三") ---> OR name NOT LIKE '%张三%'  | orNotLike(User::getName, "张三") ---> OR name NOT LIKE '%张三%' |
|like       | LIKE '%值%'     | like("name", "张三") ---> AND name LIKE '%张三%'  | like(User::getName, "张三") ---> AND name LIKE '%张三%' |
|orLike     | LIKE '%值%'     | orLike("name", "张三") ---> OR name LIKE '%张三%'  | orLike(User::getName, "张三") ---> OR name LIKE '%张三%' |
|notLike    | NOT LIKE '%值%' | notLike("name", "张三") ---> AND name NOT LIKE '%张三%'  | notLike(User::getName, "张三") ---> AND name NOT LIKE '%张三%' |
|orNotLike  | NOT LIKE '%值%' | orNotLike("name", "张三") ---> OR name NOT LIKE '%张三%'  | orNotLike(User::getName, "张三") ---> OR name NOT LIKE '%张三%' |
|leftLike     | LIKE '%值'     | leftLike("name", "张三") ---> AND name LIKE '%张三'  | leftLike(User::getName, "张三") ---> AND name LIKE '%张三' |
|orLeftLike   | LIKE '%值'     | orLeftLike("name", "张三") ---> OR name LIKE '%张三'  | orLeftLike(User::getName, "张三") ---> OR name LIKE '%张三' |
|notLeftLike  | NOT LIKE '%值' | notLeftLike("name", "张三") ---> AND name NOT LIKE '%张三'  | notLeftLike(User::getName, "张三") ---> AND name NOT LIKE '%张三' |
|orNotLeftLike| NOT LIKE '%值' | orNotLeftLike("name", "张三") ---> OR name NOT LIKE '%张三'  | orNotLeftLike(User::getName, "张三") ---> OR name NOT LIKE '%张三' |
|rightLike    | LIKE '值%'     | rightLike("name", "张三") ---> AND name LIKE '张三%'  | rightLike(User::getName, "张三") ---> AND name LIKE '张三%' |
|orRightLike  | LIKE '值%'     | orRightLike("name", "张三") ---> OR name LIKE '张三%'  | orRightLike(User::getName, "张三") ---> OR name LIKE '张三%' |
|notRightLike | NOT LIKE '值%' | notRightLike("name", "张三") ---> AND name NOT LIKE '张三%'  | notRightLike(User::getName, "张三") ---> AND name NOT LIKE '张三%' |
|orNotRightLike | NOT LIKE '值%' | orNotRightLike("name", "张三") ---> OR name NOT LIKE '张三%'  | orNotRightLike(User::getName, "张三") ---> OR name NOT LIKE '张三%' |
|orderBy    |排序，true=desc| orderBy("name", true) ---> ORDER BY name DESC  | orderBy(User::getName, true) ---> ORDER BY name DESC|
|orderBy    |排序，false=asc| orderBy("name") ---> ORDER BY name | orderBy(User::getName) ---> ORDER BY name |
|or         |OR 嵌套| or(new Criteria().eq("name", "张三").lt("age", 18)) ---> OR (name = '张三' AND age < 18) | or(new LambdaCriteria().eq(User::getName, "张三").lt(User::getAge, 18)) ---> OR (name = '张三' AND age < 18) |
|and        |AND 嵌套| and(new Criteria().eq("name", "张三").lt("age", 18)) ---> AND (name = '张三' AND age < 18) | and(new LambdaCriteria().eq(User::getName, "张三").lt(User::getAge, 18)) ---> AND (name = '张三' AND age < 18) |

## 5、一些示例

1.  查询操作
```java
@Autowired
private ProjectDao projectDao;

// 查询所以的项目，返回列表
List<Project> projectList = projectDao.select("select * from t_project_info order by created_at desc");

// 查询所以的项目，返回Map列表
List<Map<String, Object>> projectList = projectDao.selectMap("select * from t_project_info order by created_at desc");

// 查询id=1的项目，返回列表
List<Project> projectList = projectDao.select("select * from t_project_info where id = ? ", 1);

// 模糊查询项目，返回列表
List<Project> projectList =  projectDao.select("select * from t_project_info where project_name like CONCAT('%', ?, '%')", "测试项目");

// 查询id=1的项目，返回对象
Project project = projectDao.selectOne("select * from t_project_info where id = ? ", 1);

// 查询记录数
Integer count = projectDao.selectOneColumn("select count(*) from t_project_info order by created_at desc", Integer.class));

// 分页查询id>100的记录，第一页，每页10个
Page<Project> page = projectDao.paginate("select * from t_project_info order by created_at desc where id > ?", 1, 10, 100));

// 查询id=3的项目信息列表
Project project = new Project();
project.setId(3L);
List<Project> projectList = projectDao.select(project)

// 查询id=3的项目信息
Project project = new Project();
project.setId(3L);
Project project = projectDao.selectOne(project);

// 查询id=3的项目信息
Project project = projectDao.selectById(3L);

// 分页查询id=3的项目信息，第一页，每页10个
Project project = new Project();
project.setId(3L);
Page<Project> page = projectDao.paginate(project, 1, 10)

```
2.  新增操作
```java
@Autowired
private ProjectDao projectDao;

// 使用sql插入一条数据
int result = projectDao.insert("insert t_into project_info(project_name, del_flag, remark) values (?,?,?)", "测试项目", 1, "XXXXXXX");

Project project = new Project();
project.setProjectName("xxxx");
project.setDelFlag(1);
project.setCreatedBy("admin");
project.setRemark("XXXX");
// 使用实体类插入一条数据，默认忽略null
int result = baseDao.insert(project);

// 使用实体类插入一条数据，不忽略null
int result = projectDao.insert(project, false);

```

3.  更新操作
```java
@Autowired
private BaseDao baseDao;

// 使用sql插入一条数据
int result = baseDao.update(""update project_info set project_name = ? where id = ?"", new Object[]{"测试项目", 1});

Project project = new Project();
project.setId(1);
project.setProjectName("xxxx");
project.setDelFlag(1);
project.setCreatedBy("admin");
project.setRemark("XXXX");
// 使用实体类更新一条数据，默认忽略null
int result = baseDao.updateById(project);

// 使用实体类更新一条数据，不忽略null
int result = baseDao.updateById(project, false);

``` 


4.  删除操作
```java
@Autowired
private BaseDao baseDao;

// 使用sql插入一条数据
int result = baseDao.delete(""delete from project_info where id = ?"", new Object[]{1});

Project project = new Project();
project.setId(1);
// 使用实体类删除一条数据，默认忽略null
int result = baseDao.delete(project);

// 根据id删除一条数据
int result = baseDao.deleteById(1, Project.class);

``` 