# Spring Data Gremlin Design

### Orientation

Gremlin is a functional, data-flow language that enables users to succinctly express complex 
traversals on (or queries of) their application's property graph. It hides the details of backend
database implementation (like azure cosmosdb support Graph API).

Apache Tinkerpop gremlin java driver allows you to launch gremlin query, but it is not easy to
get familiar with gremlin syntax. You have to generate all gremlin queries by yourself as following.

```java
    static final String[] gremlinQueries = new String[] {
        "g.V().drop()",
        "g.addV('person').property('id', '1').property('name', 'pli').property('age', 31)",
        "g.addV('person').property('id', '4').property('name', 'incarnation').property('age', 27)",
        "g.addV('software').property('id', '2').property('name', 'spring-boot-sample').property('lang', 'java')",
        "g.V('1').addE('created').to(g.V('2')).property('weight', 0.8)",
        "g.V('1').addE('contributed').to(g.V('2')).property('weight', 0.1)",
        "g.V('4').addE('contributed').to(g.V('2')).property('weight', 0.4)"
    };
```

We'd like to make things easier by hiding the process of mapping Java instance to graph database
persistent entity, with the help of spring-data-commons.

### From Users' View
How do users use graph db? They can use gremlin driver to generate the query literal in Java class instance.
It's not impossible but yet isn't easy. We want to figure out a more Spring natural way by leveraging
Spring annotations to map Java instance to database entity.

### From Graph database View
As we know, there may be some concept like `Vertex`, `Edge` and `Graph` when we talk about 
graph. Naturally the object instance needs to be mapped to one and the only one of these
element in graph. Simply we add some annotations to reach this.

```java
  @Vertex    // maps an Object to a Vertex
  @VertexSet // maps a set of Vertex in Graph
  @Edge      // maps an Object to an Edge
  @EdgeSet   // maps to a set of Edge in Graph
  @EdgeFrom  // maps to the head Vertex of an Edge
  @EdgeTo    // maps to the tail Vertex of an Edge
  @Graph     // maps to an Object to a Graph
```

### CRUD based query
The `@GremlinRepository` extends `@CrudReposiotry` is providing basic queries, like insert,
save, find, delete and count. Let's take insert as example to the details of implementation.

##### Some Constrictions
* Gremlin describes the `Vertex` and `Edge` in a flat layout, with fixed property `id`, `label`,
and any other properties organized as key-value pair.
* Gremlin properties name must be `String`, and values can be `Number`, `Boolean` and `String`.
* No nested structure in `Vertex` and `Edge`.
* `Edge` is directed.

##### GremlinSource
Before we start to insert a `Vertex` instance to database, we need one abstract layer into
isolate the dependency between instance and entity in database. We define one class to
represent all instance stored in database. This class not only needs to hold all information from
instance object, but also has the flat structure like entity in database. It is the bridge between
instance in java and persistent entity in database. Here we call it `GremlinSource`.

With the above constriction of gremlin, `GremlinSource` has field `id` and `label` for mapping,
and keep all other fields into one `Map<String, Object>`. When we try to insert a instance from
java to `GremlinSource`, we perform a **WRITE** operation and convert the instance to a `GremlinSource`.
Operation **WRITE** will convert all the data of one instance to `GremlinSource`, and take care of 
`id` or `@Id` field.

The `GremlinSourceWriter` converts Java instance to `GremlinSource`.

##### GremlinResult
There must be one **READ** operation for retrieving instance from database entity. For example,
the `find` query will return the persistent data from database with the type `Result`, provided
by apache SDK. For insulating the dependency between instance and `Result`, just like what we do
in **WRITE**. We also use `GremlinSource` as the bridge from `Result` to instance. Simply there
are two steps after query from database. First the `Result` from database will be converted to
`GremlinSource`. And then the `GremlinSource` will be converted to Java instance, just like what
we do in **WRITE** operation.

The `GremlinResultReader` converts `Result` to `GremlinSource`.   
The `GremlinSourceReader` converts `GremlinSource` to Java instance.

##### GremlinScript
GremlinScript will generate the query based on `GremlinSource`. It converts the data like
`id`, `label` and `properites` into gremlin query literal Strings. For type `String`, `Number`
and `Boolean`, the query will store them as primitive types (supported by gremlin). And any
other type of instance field will be converted to Json-like `String` except type `Date`, it will
be converted to milliSeconds and stored as `Number`. Then the gremlin client will execute the query
to access database.

The `GremlinScriptLiteral` generates literal query based on `GremlinSource`.
