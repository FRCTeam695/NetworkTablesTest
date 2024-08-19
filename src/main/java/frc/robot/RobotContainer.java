// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;


import java.util.function.DoubleSupplier;

import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.DoubleSubscriber;
import edu.wpi.first.networktables.DoubleTopic;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.Trigger;

public class RobotContainer {
  private final GenericHID logitechGamepad = new GenericHID(0);

  // see for more info: https://docs.wpilib.org/en/stable/docs/software/networktables/networktables-intro.html

  // the NetworkTableInstance is going to be the same for the entire robot code project
  private final NetworkTableInstance inst = NetworkTableInstance.getDefault();
  
  // the NetworkTable is probably a per-subsystem item
  // e.g., an elevator, arm, or shooter would get a table to itself
  // otherwise, everything ends up flat inside the SmartDashboard table
  private final NetworkTable axis0TestTable = inst.getTable("Axis0Test");

  // topics are the entries within a table, but there are no topic objects stored here
  // instead, they are used immediately to construct publishers or subscribers

  // a publisher is for an output value, such as the position of the elevator/arm/etc...
  // it's okay to not have a default value because it is going to be read by a human
  private final DoublePublisher axis0Pub = axis0TestTable.getDoubleTopic("Axis0Multiplied").publish();


  // a subscriber is for an input value, such as a tuning constant (e.g., kP, kF, offset)

  // using a subscriber is preferable to SmartDashboard.putNumber/getNumber because the
  // entry name (String) appears only once; after that, it's the object reference that
  // appears in code.  That way, the compiler can verify that it exists, instead of
  // discovering a typo at runtime.  

  // a subscriber should have a default value, since the robot will start up and need
  // some sort of default before a human has a chance to enter all of the constants

  // it is also best if the robot publishes all of the topic names it expects, since
  // otherwise, a human would need to create those topics and enter the names properly
  // after every robot reboot

  // ....however, a subscriber never creates a topic or publishes to it, so some extra
  // code is required; see subscribeWithDefault's comment below
  private final DoubleSubscriber multGetter = subscribeWithDefault(axis0TestTable, "Multiplier", 1.0);
  
  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {

    // Configure the trigger bindings
    configureBindings();
  }

  // Utility function to fix the behavior of DoubleTopic.subscribe()
  // The behavior of subscribe() is not helpful when, say, tuning a PID loop.  
  // In this case, the goal is:
  // 1. setup code publishes an entry in the table (which makes the name visible)
  // 2. setup code sets that entry to a default value
  // 3. the programmer/tuner edits that value
  // 4. the program runs and gets the edited value, and uses it
  // 5. repeat steps 3-5
  // (this function should go in a utility class somewhere)
  public static DoubleSubscriber subscribeWithDefault(NetworkTable table, String name, double defVal)
  {
    DoubleTopic topic = table.getDoubleTopic(name);
    DoubleSubscriber sub = topic.subscribe(defVal); // this does NOT publish defVal

    // at this point, there will be no entry in the table with <name>

    topic.publish().set(sub.get());

    // now there will be an entry with <name> and value <defVal>

    return sub;
  }

  private void configureBindings()
  {
    Trigger button1 = new Trigger(() -> logitechGamepad.getRawButton(1));

    button1.whileTrue(publishAxisZeroMultiplied(logitechGamepad, axis0Pub, multGetter::get));
  }

  private static Command publishAxisZeroMultiplied(GenericHID hid, DoublePublisher pub, DoubleSupplier mult)
  {
    return Commands.run(() -> {pub.set(mult.getAsDouble() * hid.getRawAxis(0));});
  }

  public Command getAutonomousCommand() {
    // An example command will be run in autonomous
    return new WaitCommand(0);
  }
}
