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
val testTree = "parent" / { "child" //  {"grandchild" replyTo self} }
```

then call 

```
testTree.materialize
```

which is a blocking call, so can wait for until the hierarchy is created

call to 
```scala 
materialize
``` 
will return ActorRef for Hierarchy's root actor

Hierarchy can be destroyed when no more needed by calling

```scala 
shutdownGracefully(rootActorRef)
``` 
of 
```scala 
trait GracefulShutdown
``` 
which needs to be mixed in to your specs

## Using default implementation

With "replyTo" use "withImplementation" and pass actor.Receive to it to add additional custom implementation

####Example
Sends both "PONG"(custom part) and actual message

```scala
"parent" / {
        "child" replyTo self withImplementation {
          case message: Any =>
            self ! "PONG"
        }
      }
```

####TODO

add creation of actors from Props into DSL


#### Usage:

Here is the simple example of how to use the library in your tests

## Imports

Mix in 'GracefulShutdown' trait into your test spec (your tests should extend 'akka.testkit.TestKit')
this will allow you to gracefully shutdown created spy hierarchy after the test

## Create mocks

before create and materialize spy hierarchy it's better to initialize some listener for certain paths

```scala
val grandChildListener = TestProbe()
```

it might be necessary to wait until probe/probes are created with 'expectNoMsg()'.
Then, create test hierarchy

```scala
      val rootRef = ("root" / {
        "parent" / {
          ("child-1".replyTo(self) / {
            "grand-child-1" replyTo grandChildListener.ref
          }) ~ ("child-2" replyTo self) ~ ("child-3" replyTo self)
        }
      }).materialize
```

the following snipped will create actors under the following paths:

```
"user/root"
"user/root/parent"
"user/root/parent/child-1"
"user/root/parent/child-1/grand-child-1"
"user/root/parent/child-2"
"user/root/parent/child-3"
```

Notice that the actor under "user/root/parent/child-1/grand-child-1" 
will proxy the messages it gets to 'grandChildListener' probe's ref we have created earlier.

##Sending messages

Lets send simple message to the 'grand-child-1'
```scala
context.actorSelection(context.actorSelection(""user/root/parent/child-1/grand-child-1") ! "Ping"
```
##Checking results

```scala
val response = grandChildListener.expectResponse[String]
response.message shouldBe "Ping"
```

or, specifying the expected path

```scala
grandChildListener.expectResponse[String]("/root/parent/child-1/grand-child-1").get.message shouldBe "Ping"
```

here, Response is parametrized



