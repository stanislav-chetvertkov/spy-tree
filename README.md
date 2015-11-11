SpyTree
=========================

This project is a DSL used for testing Actor systems.
It allows creation of complex actor hierarchies 
with ability to listen messages passed to actors which are the part of it.

Usage
=========================

It can be handy to test 'actorSelection' calls when it has nested structure
like /parent/child/grandchild and so on

```scala 
import com.github.spytree.ActorListenersDSL._
val testTree = "parent" >> { "child" >>  {"grandchild" replyTo self} }
```

then call 

```scala 
testTree.materialize``` 
which is a blocking call, so can wait for until the hierarchy is created

call to 
```scala materialize``` 
will return ActorRef for Hierarchy's root actor

Hierarchy can be destroyed when no more needed by calling

```scala shutdownGracefully(rootActorRef)``` of ```scala trait GracefulShutdown``` which needs to be mixed in to your specs

## Using default implementation

With "replyTo" use "withImplementation" and pass actor.Receive to it to add additional custom implementation

####Example
Sends both "PONG"(custom part) and actual message

```scala
"parent" >> {
        "child" replyTo self withImplementation {
          case message: Any =>
            self ! "PONG"
        }
      }
```

####TODO

add creation of actors from Props into DSL