# TrainManager

TrainManager is a Minecraft Forge 1.16.5 mod that works with Immersive Railroading. It allows the creation of routes, each of which can have station stops and speed limit changes. Trains can be assigned to a route, and when enabled, they will travel the route in sequence, hitting station stops and making speed limit changes.

Currently everything is managed through commands, but I'll probably be adding a GUI at some point (especially to allow re-ordering of stops in the case they are added out of sequence).

## Download

[You can download the jar here](https://github.com/lucaslower/TrainManager/blob/main/build/libs/trainmanager-1.0.0.jar) (it's in the build/libs directory)

I will put it on CurseForge once I've got it a bit more updated.

## Command Usage

The base command is /trainmanager. This sucks to type, so let me know if you can think of anything better. I was going to do /tm but that's a built-in command, womp womp.

### Route Commands

- */trainmanager routes*
  
  Lists currently registered routes.
- */trainmanager routes create \<routeName\>*
  
  Creates a new route. The route name cannot contain spaces, it must be a single string. The name is used to attach trains to the route and to edit the route.
- */trainmanager routes \<routeName\> addStop \<stationName\>*
  
  Creates a new stop on the route. Station name must also contain no spaces. When you run this command, you want to be at the position on the track where you want the lead locomotive of the train to stop. In 99% of cases the middle of the locomotive will stop where you create the station stop.
- */trainmanager routes \<routeName\> addSpeedChange \<speed\>*

  Creates a new speed limit change on the route. The speed is a float value in MPH. I'll add support for different units soon. When you run this command, you want to be at the position on the track where you want the speed limit to take effect. If a speed limit decrease, the locomotive will begin slowing to the new speed limit before reached, so that it reaches the limit at the position you created the limit. The train starts accelerating at this point if it is a speed limit increase.

#### Note on stop/speed change ordering

  A thing to note for both adding stations and speed changes is that (currently) you need to add them in the order the train will travel through them. When I have needed to re-order things, I've used [NBT Explorer](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-tools/1262665-nbtexplorer-nbt-editor-for-windows-and-mac) to reorder the entries in the list. In NBT Explorer, if you open your world folder, then the data folder, you'll see "trainmanager-SaveData.dat". Open that, open the "data" item, then you'll see "routes" and "trains". In the "routes" compound, you'll see the routes you have created, starting with "route0". Inside that, you'll see a list called "targets" which holds all stops and speed changes. To re-order them you can right click and select Move Up / Move Down -- I will be making a GUI to do this ingame. 

### Train Commands

- */trainmanager trains*

  Lists currently registered trains.
- */trainmanager trains create \<trainName\> \<leadLoco\> \<routeName\>*

  Creates a train with the label *trainName* (which cannot have spaces), and assigns it to the route *routeName*. The *leadLoco* is an entity argument, and is the UUID of the lead locomotive. The easiest way to get this UUID is to be inside the locomotive when you run this command, and the UUID will be one of the suggested values you can use tab to complete. This UUID is also displayed by the train radio control card in game, or you can get it by looking at the locomotive, pressing F3+I, and getting it out of the copied data.
- */trainmanager trains \<trainName\> enable*

  Enables the train (turns it on, reverser to forward). The train will begin speeding up towards the next target, either a station stop or a speed limit change.
- */trainmanager trains \<trainName\> disable*

  Disables the train. Throttle to 0, brakes to full, reverser neutral, and engine off.
- */trainmanager trains \<trainName\> setLoco \<leadLoco\>*

  Sets the lead locomotive for *trainName*. *leadLoco* argument works as described in the create command.
- */trainmanager trains \<trainName\> setRoute \<routeName\>*

  Sets the route to *routeName* for *trainName*.
- */trainmanager trains \<trainName\> setNextTarget <nextTargetNum>*

  This sets the next target for the train to the given number. This number is the same as the one in the NBT compound as described above in the note on stop/speed change ordering. This is useful when you create a train and it's already somewhere within the route target sequence.
- */trainmanager trains \<trainName\> saveData \<true/false\>*

  Sets whether or not the train saves some stats to a CSV file. This includes acceleration, the current action it is doing, etc. I got to make some fun graphs with this, so it was worth it.
- */trainmanager trains \<trainName\> broadcastUpdates \<true/false\>*

  Sets whether or not the train sends updates in chat, such as what it is currently doing speed limit wise, station stops, etc.
