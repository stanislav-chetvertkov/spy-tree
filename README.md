SpyTree
=========================

This project is a DSL used for testing Actor systems.
It allows creation of complex actor hierarchies 
with ability to listen messages passed to actors which are the part of it.

Usage
=========================

It can be handy to test 'actorSelection' calls when it has nested structure
like /parent/child/grandchild and so on

val testTree = "parent" >> { "child" >>  {"grandchild" replyTo self} }

then call 

testTree.materialize which is a blocking call, so can wait for until the hierarchy is created

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