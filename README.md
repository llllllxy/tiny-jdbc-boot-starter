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

## 1、介绍
tiny-jdbc-boot-starter是一个基于Spring JdbcTemplate 开发的轻量级数据库ORM工具包，同时支持sql操作、实体类映射操作，让操作数据库这件事变得更加简单！

## 2、使用
### 2.1、引入依赖
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
### 2.2、配置项
#### 2.2.1 全局配置
```yaml
tiny-jdbc:
  # 数据库分页类型，目前支持三种(mysql,oracle,db2)
  db-type: mysql
```

#### 2.2.2 注解说明
##### @Table
- 描述：表名注解，标识实体类对应的表
- 使用位置：实体类
```java
@Table("b_upload_file")
public class UploadFile implements Serializable {
    private static final long serialVersionUID = -1L;
    
    ...
}
```
##### @Column
- 描述：字段注解
- 使用位置：实体类
```java
@Table("b_upload_file")
public class UploadFile implements Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * 主键，三种主键策略互斥，只能选择其中一种
     * 注意，如果设置为自增主键的话，则此字段必须为Long
     * 如果设置为assignUuid的话，则此字段必须为String
     * 如果设置为assignId的话，则此字段必须为String或者Long
     */
    @Column(value = "id", primaryKey = true, assignId = true)
    private Long id;
    
    @Column("file_id")
    private String fileId;
    
    @Column("file_name_old")
    private String fileNameOld;
}
```
|属性|类型|必须指定|默认值|描述|
|---|---|---|---|---|
| value         | String  |  是 | ""    | 对应数据库字段名  |
| primaryKey    | boolean |  否 | false | 是否为主键  |
| autoIncrement | boolean |  否 | false | 主键策略：自增主键，三种主键策略互斥，只能选择其一 |   
| assignId      | boolean |  否 | false | 主键策略：雪花ID，三种主键策略互斥，只能选择其一  |
| assignUuid    | boolean |  否 | false | 主键策略：UUID，三种主键策略互斥，只能选择其一 |


### 2.3、使用说明
#### 2.3.1、注入BaseDao
```java
    @Autowired
    private BaseDao baseDao;
```
#### 2.3.2、查询操作
|方法|说明|
|---|---|
|`<T> List<T> select(String sql, Class<T> classz, Object... params);` |根据给定的sql和实体类型和参数，查询数据库并返回实体类对象列表|
|`List<Map<String, Object>> select(String sql, Object... params);`|根据给定的sql和参数，查询数据库并返回Map<String, Object>列表|
|`List<Map<String, Object>> select(String sql);`|根据给定的sql，，查询数据库并返回Map<String, Object>列表|
|`<T> T selectOne(String sql, Class<T> classz, Object... params);`|根据给定的sql和实体类型和参数，查询数据并返回一个实体类对象|
|`Map<String, Object> selectOne(String sql, Object... params);`|根据给定的sql和参数，查询数据并返回一个Map<String, Object>对象|
|`Map<String, Object> selectOne(String sql);`|根据给定的sql，，查询数据库并返回一个Map<String, Object>对象|
|`<T> T selectOneColumn(String sql, Class<T> clazz, Object... params);`|根据给定的sql和实体类型和参数，查询数据并返回一个值（常用于查count）|
|`<T> Page<T> paginate(String sql, Class<T> clazz, Integer pageNumber, Integer pageSize);`|执行分页查询，返回Page对象|
|`<T> Page<T> paginate(String sql, Class<T> clazz, Integer pageNumber, Integer pageSize, Object... params);`|执行分页查询，返回Page对象|
|`<T> T selectById(Object id, Class<T> classz);`|根据主键ID值，查询数据并返回一个实体类对象|
|`<T> List<T> select(T entity);`|实体类里面非null的属性作为查询条件，查询数据库并返回实体类对象列表|
|`<T> Page<T> paginate(T entity, Integer pageNumber, Integer pageSize);`|实体类里面非null的属性作为查询条件，执行分页查询|
|`<T> T selectOne(T entity);`|实体类里面非null的属性作为查询条件，查询数据并返回一个实体类对象|

#### 2.3.3、插入操作
|方法|说明|
|---|---|
|`int insert(String sql, final Object... params);`|根据提供的SQL语句和提供的参数，执行插入|
|`<T> int insert(T entity);`|插入entity里的数据，将忽略entity里属性值为null的属性，如果主键策略为assignId或assignUuid，那将在entity里返回生成的主键值|
|`<T> int insert(T entity, boolean ignoreNulls);`|插入entity里的数据，可选择是否忽略entity里属性值为null的属性，如果主键策略为assignId或assignUuid，那将在entity里返回生成的主键值|
|`<T> Long insertReturnAutoIncrement(T entity);`|插入entity里的数据，将忽略entity里属性值为null的属性，并且返回自增的主键|

#### 2.3.4、修改操作
|方法|说明|
|---|---|
|`int update(String sql, final Object... params);`|根据提供的SQL语句和提供的参数，执行修改|
|`<T> int updateById(T entity);`|根据主键值更新数据，将忽略entity里属性值为null的属性|
|`<T> int updateById(T entity, boolean ignoreNulls);`|根据主键值更新数据，可选择是否忽略entity里属性值为null的属性|

#### 2.3.5、删除操作
|方法|说明|
|---|---|
|`int delete(String sql, final Object... params);` | 根据提供的SQL语句和提供的参数，执行删除|
|`<T> int deleteById(Object id, Class<T> clazz);` | 根据主键ID进行删除 |
|`<T> int delete(T entity);`| 根据entity里的属性值进行删除，entity里不为null的属性，将作为参数 |



## 3、示例

1.  查询操作
```java
@Autowired
private BaseDao baseDao;

// 查询所以的项目，返回列表
List<Project> projectList = baseDao.select("select * from t_project_info order by created_at desc", Project.class);

// 查询所以的项目，返回Map列表
List<Map<String, Object>> projectList = baseDao.select("select * from t_project_info order by created_at desc");

// 查询id=1的项目，返回列表
List<Project> projectList = baseDao.select("select * from t_project_info where id = ? ", Project.class, 1);

// 模糊查询项目，返回列表
List<Project> projectList =  baseDao.select("select * from t_project_info where project_name like CONCAT('%', ?, '%')",  Project.class, "测试项目");

// 查询id=1的项目，返回对象
Project project = baseDao.selectOne("select * from t_project_info where id = ? ", Project.class, 1);

// 查询记录数
Integer count = baseDao.selectOneColumn("select count(*) from t_project_info order by created_at desc", Integer.class));

// 分页查询id>100的记录，第一页，每页10个
Page<Project> page = baseDao.paginate("select * from t_project_info order by created_at desc where id > ?", Project.class, 1, 10, 100));

// 查询id=3的项目信息列表
Project project = new Project();
project.setId(3L);
List<Project> projectList = baseDao.select(project)

// 查询id=3的项目信息
Project project = new Project();
project.setId(3L);
Project project = baseDao.selectOne(project);

// 查询id=3的项目信息
Project project = baseDao.selectById(3L, Project.class);

// 分页查询id=3的项目信息，第一页，每页10个
Project project = new Project();
project.setId(3L);
Page<Project> page = baseDao.paginate(project, 1, 10)

```
2.  新增操作
```java
@Autowired
private BaseDao baseDao;

// 使用sql插入一条数据
int result = baseDao.insert("insert t_into project_info(project_name, del_flag, remark) values (?,?,?)", "测试项目", 1, "XXXXXXX");

Project project = new Project();
project.setProjectName("xxxx");
project.setDelFlag(1);
project.setCreatedBy("admin");
project.setRemark("XXXX");
// 使用实体类插入一条数据，默认忽略null
int result = baseDao.insert(project);

// 使用实体类插入一条数据，不忽略null
int result = baseDao.insert(project, false);

```

3.  更新操作
    


4.  删除操作
