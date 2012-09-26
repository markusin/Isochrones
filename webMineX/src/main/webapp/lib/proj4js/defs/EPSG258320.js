/*
 * This is the projection format used in the province Bozen/Bolzano /South-Tyrol/Italy.
 * The format is very similar to EPSG:25832
 * 
 *  
 **  Human-Readable OGC WKT **
 PROJCS["ETRF_1989_UTM_Zone_32N",
  GEOGCS["GCS_ETRF_1989",
   DATUM["D_ETRF_1989",
    SPHEROID["WGS_1984",6378137.0,298.257223563]
   ],
   PRIMEM["Greenwich",0.0],
   UNIT["Degree",0.0174532925199433]
  ],
  PROJECTION["Transverse_Mercator"],
  PARAMETER["False_Easting",500000.0],
  PARAMETER["False_Northing",0.0],
  PARAMETER["Central_Meridian",9.0],
  PARAMETER["Scale_Factor",0.9996],
  PARAMETER["Latitude_Of_Origin",0.0],
  UNIT["Meter",1.0]
 ]
 *
 ** Postgis insert statement
 INSERT into spatial_ref_sys (srid, auth_name, auth_srid, proj4text, srtext) values ( 9258320, 'spatialreference.org', 258320, '+proj=utm +zone=32 +ellps=WGS84 +lat_0=0 +lon_0=21.45233333333333 +k=0.999600 +x_0=500000.0 +y_0=0 +pm=greenwich +units=m', 'PROJCS["ETRF_1989_UTM_Zone_32N",GEOGCS["GCS_ETRF_1989",DATUM["D_ETRF_1989",SPHEROID["WGS_1984",6378137.0,298.257223563]],PRIMEM["Greenwich",0.0],UNIT["Degree",0.0174532925199433]],PROJECTION["Transverse_Mercator"],PARAMETER["False_Easting",500000.0],PARAMETER["False_Northing",0.0],PARAMETER["Central_Meridian",9.0],PARAMETER["Scale_Factor",0.9996],PARAMETER["Latitude_Of_Origin",0.0],UNIT["Meter",1.0]]');
 *
 ** Proj4js format **
 */
Proj4js.defs["EPSG:258320"] = "\
  +title=ETRF_1989_UTM_Zone_32N\
  +proj=utm\
  +zone=32\
	+ellps=WGS84\
	+lat_0=0\
	+lon_0=21.45233333333333\
	+k=0.999600\
	+x_0=500000.0\
	+y_0=0\
	+pm=greenwich\
	+units=m\
 ";
 
 //+title=ETRF_1989_UTM_Zone_32N +proj=utm +zone=32 +ellps=WGS84 +lat_0=0 +lon_0=21.45233333333333 +k=0.999600 +x_0=500000.0 +y_0=0 +pm=greenwich +units=m +towgs84=-104.1,-49.1,-9.9,-0.971,-2.917,0.714,-11.68 +no_defs