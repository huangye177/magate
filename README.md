MaGate
======

An easy-to-use simulator for decentralized distributed scheduling in Clusters, Grids, and Cloud ecosystem

======
Features:

* Decentralized distributed scheduling environment

* Real grid workload trace archive supported

* Graphic intuitive dynamic tracing system

* Simplified Community-Aware Scheduling Algorithm integrated

* Self-structured P2P overlay infrastructure simulator integrated

More research background info: http://gridgroup.hefr.ch/smartgrid/doku.php?id=masim

======
Quick Start:

Run with source code to check the simulation out directly (after git clone, under project root directory)

./gradlew clean build run

(NOTICE: the first build takes sometime to download the input job archives; more details about Input Job Archives Download is detailed below)

======
Simulated Grid Scenario by MaGate:

The MaGate uses job submission archives of real grids in order to deliver comparable after-scheduling results, which are produced under using scenarios as close to the real world (thanks work of The Grid Workloads Archive: http://gwa.ewi.tudelft.nl/pmwiki/pmwiki.php).

In particular, following grid scenarios (with job input archives) can be simulated for MaGate:

Grid5000 (default scenario)
(workload date: 1 Apr. 2010; 9 sites, 26 clusters, 3194 CPUs; submitted jobs: 1020195)

AuverGrid 
(workload date: Jan. 2006 - Jan. 2007; 5 clusters, 475 CPUs; submitted jobs: 404176)

NorduGrid
(workload date: 2004-2006 (unclear); 68 clusters, 4454 CPUs; submitted jobs: 781370)

SHARCNET
(workload date: Dec. 2005 - Jan. 2007; 10 clusters, 6828 CPUs; submitted jobs: 1195242)

Above job archives need to be downloaded for at least one time into the project input directory; for more details, check the content below:

======
Notice of Input Job Archives Download:

By default, MaGate will try to download the needed job archives automatically (as below):

GWA-Grid5000.db3

GWA-AuverGrid.db3

GWA-NorduGrid.db3

GWA-SHARCNET.db3

therefore you do not need to bother with the input job preparation. 

If you already have above files, or you did the download by yourself in advance, then you can copy them into the MaGate input file directory to stop the download process,  <MaGate_PROJECT_DIRECTORY>/magateinput/workloadtrace

The downloads are available via:

http://sourceforge.net/projects/magate/files/GWA%20Traces/

or

https://www.dropbox.com/sh/7e31810wxnf1cba/_jTf9Ruc_M

Alternatively, direct download links are also available:

Grid5000 (default)
https://dl.dropboxusercontent.com/s/ebd352pj45yf8rz/GWA-Grid5000.db3?dl=1&token_hash=AAH91Hq3vYti0GUW9OaBiWjUf4mkJiTIeEyoMJqLFC91bA

AuverGrid
https://dl.dropboxusercontent.com/s/e3oi34dvla39vs4/GWA-AuverGrid.db3?dl=1&token_hash=AAF8P0UwEDlOt4f4jmk3IBFZjtVfwaj1L4l9_jAjTvfj4A

NorduGrid
https://dl.dropboxusercontent.com/s/gy9edul4596mgmo/GWA-NorduGrid.db3?dl=1&token_hash=AAHpRmuJs6Qf_8859ts8MPwpPNFnbuXXoRaNp_OVXNJNdg

SHARCNET
https://dl.dropboxusercontent.com/s/57013qxsnlzgm52/GWA-SHARCNET.db3?dl=1&token_hash=AAErG0nJfFgwlBVB_Lk2TbO89Z2D_7TUg4_R0P3OmQgbOw









