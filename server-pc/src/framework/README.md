This is a framework for remote NXT communications allowing for use of un-regulated motors.

The most important idea of this framework is the concept of a `Test`, which is an abstract class.

A `Test` is a unit of instructions to the brick. It may be random power settings to collect data, an attempt to set a certain speed, etc. 

`testLength` is a protected field, implying that implementing classes may wish to change it. It is the length in milliseconds that a test lasts. Controllers will generally want longer a longer `testLength`, especially if you wish it to maintain a speed for a while as you cause disturbances to the system. 

The `abstract protected` method `test` must be implemented by sub classes, and is the action which occurs during a `Test`. 

This class is a `BrickListener`, a concept explained in the README of the `communications` package. The brick updates are used to collect data. Controllers should themselves be `BrickListener`s as well because most updates will be discarded by the `Test`, whereas a `Controller` may want all of them. For example a control loop may occur every 100ms, whereas a test loop may occur every 2000ms.

