#Todos

###Features
Seperate Browser/File Manager from Player/Inspector UI?

Change the GUI System so that your Components get a "Renderer" 

Browser: manage files, add songs, name projects, see all files
Player: see details, current queue, notes, timestamps

Data Editor: Popup Form?

Fuse the Browser to have a 2 col layout in one Rack

.________________RACK______________________________________________________. 
|   ____________________________________________________________________   | 
|  | Projects| Lists   |      Songs                                     |  |
|  |---▼----- _________| _______________________________________________|  |
|  |  Group Name A   > |  Song Name A                              3:45 |  |
|  |  Group Name B     |  Song Name B                              1:16 |  |
|  |                   |  Song Name C                      (2)▼    5:28 |  |
|  |                   |   - Version 2                             5:28 |  |
|  |                   |   - Version 1                             3:28 |  |
|  |                   |  When the Sun looms               (2)◀   17:12 |  |
|  | ----- Lists  ---- |  Unknown Field of ..              (8)◀    8:52 |  |
|  | List Name A       |  Songs for Moons                  (3)◀    6:12 |  |
|  | List Name B       |  Songs for Stars                  (4)▼    7:12 |  |
|  |                   |   - Version 4                             7:12 |  |
|  |                   |   - Version 3                             5:28 |  |
|  |                   |   - Version 2                             5:28 |  |
|  |                   |                                                |  |
|   ____________________________________________________________________   |
|                                                                          |
|                                                                          |
o _______________________________________________________________________ o


A "Song Cell" should always provide a context menu with actions like:
 add to .. (artist, song, list, queue) 
 play next
 show in finder
 link to Project
 remove from ... (artist, list, queue),
no matter where the songcell is. the browser AND the queue contain songcells
it should also always be draggable onto an artist, list, song, queue


###Transport
- [ ] add Volume control
