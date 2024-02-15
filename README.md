# TrainManager

TrainManager is a Minecraft Forge 1.16.5 mod that works with Immersive Railroading. It allows the creation of routes, each of which can have station stops and speed limit changes. Trains can be assigned to a route, and when enabled, they will travel the route in sequence, hitting station stops and making speed limit changes.

Currently everything is managed through commands, but I'll probably be adding a GUI at some point (especially to allow re-ordering of stops in the case they are added out of sequence).

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

  A thing to note for both adding stations and speed changes is that (currently) you need to add them in the order the train will travel through them. When I have needed to re-order things, I've used [NBT Explorer](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-tools/1262665-nbtexplorer-nbt-editor-for-windows-and-mac) to rename the NBT compounds. The order is set by the order they get loaded in when the world loads. They are loaded in numerical order. In NBT Explorer, if you open your world folder, then the data folder, you'll see "trainmanager-SaveData.dat". Open that, open the "data" item, then you'll see "routes" and "trains". In the "routes" compound, you'll see the routes you have created, starting with "route0". Inside that, you'll see a compound called "targets" which holds all stops and speed changes. They are each named "target0", "target1", "target2", etc. To re-order them you need to rename them to the right numerical order. This is super annoying since you have to basically rename everything once, then do it again. This is why I'll make a GUI to make it easier. 
