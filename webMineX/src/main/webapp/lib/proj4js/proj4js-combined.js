/*
  proj4js.js -- Javascript reprojection library. 
  
  Author:       Mike Adair madairATdmsolutions.ca
                Richard Greenwood rich@greenwoodmap.com
  License:      LGPL as per: http://www.gnu.org/copyleft/lesser.html 
                Note: This program is an almost direct port of the C library
                Proj4.
*/
/* ======================================================================
    proj4js.js
   ====================================================================== */

/*
Author:       Mike Adair madairATdmsolutions.ca
              Richard Greenwood rich@greenwoodmap.com
License:      LGPL as per: http://www.gnu.org/copyleft/lesser.html
              Note: This program is an almost direct port of the C library Proj4.
$Id: Proj.js 2956 2007-07-09 12:17:52Z steven $
*/

/**
 * Provides methods for coordinate transformations between map projections and 
 * longitude/latitude, including datum transformations.
 * 
 * Initialization of Proj objects is with a projection code, usually EPSG codes.
 * The code passed in will be stripped of colons (':') and converted to uppercase
 * for internal use.
 * If you know what map projections your application will be dealing with, the
 * definition for the projections can be included with the script tag when the 
 * application is being coded.  Otherwise, practically any projection definition
 * can be loaded dynamically at run-time with an AJAX request to a lookup service
 * such as spatialreference.org.
 * The actual code supporting the forward and inverse tansformations for each
 * projection class is loaded dynamically at run-time.  These may also be 
 * specified when the application is coded if the projections to be used are known
 * beforehand.
 * A projection object has properties for units and title strings.
 * All coordinates are handled as points which is a 2 element array where x is
 * the first element and y is the second.
 * For the transform() method pass in mapXY and a destination projection object
 * and it returns a map XY coordinate in the other projection
 */

/**
 * Global namespace object for Proj4js library to use
 */
Proj4js = {

    /**
     * Property: defaultDatum
     * The datum to use when no others a specified
     */
    defaultDatum: 'WGS84',                  //default datum

    /**
     * Property: proxyScript
     * A proxy script to execute AJAX requests in other domains. 
     */
    proxyScript: null,  //TBD: customize this for spatialreference.org output

    /**
     * Property: defsLookupService
     * AJAX service to retreive projection definition parameters from
     */
    defsLookupService: 'http://spatialreference.org/ref',

    /**
     * Property: libPath
     * internal: http server path to library code.
     * TBD figure this out automatically
     */
    libPath: '../lib/',

    /** 
    * Method: transform(source, dest, point)
    * Transform a point coordinate from one map projection to another.
    *
    * Parameters:
    * source - {Proj4js.Proj} source map projection for the transformation
    * dest - {Proj4js.Proj} destination map projection for the transformation
    * point - {Object} point to transform, may be geodetic (long, lat) or
    *     projected Cartesian (x,y), but should always have x,y properties.
    */
    transform : function(source, dest, point) {
        if (!source.readyToUse || !dest.readyToUse) {
            this.reportError("Proj4js initialization for "+source.srsCode+" not yet complete");
            return;
        }
        
        if (point.transformed) {
          this.log("point already transformed");
          return;
        }
        
        // Workaround for Spherical Mercator
        if ((source.srsProjNumber =="900913" && dest.datumCode != "WGS84") ||
            (dest.srsProjNumber == "900913" && source.datumCode != "WGS84")) {
            var wgs84 = Proj4js.WGS84;
            this.transform(source, wgs84, point);
            point.transformed = false;
            source = wgs84;
        }

        // Transform source points to long/lat, if they aren't already.
        if ( source.projName=="longlat") {
            point.x *= Proj4js.common.D2R;  // convert degrees to radians
            point.y *= Proj4js.common.D2R;
        } else {
            if (source.to_meter) {
                point.x *= source.to_meter;
                point.y *= source.to_meter;
            }
            source.inverse(point); // Convert Cartesian to longlat
        }

        // Adjust for the prime meridian if necessary
        if (source.from_greenwich) { 
            point.x += source.from_greenwich; 
        }

        // Convert datums if needed, and if possible.
        point = this.datum_transform( source.datum, dest.datum, point );

        // Adjust for the prime meridian if necessary
        if (dest.from_greenwich) { 
            point.x -= dest.from_greenwich; 
        }

        if( dest.projName=="longlat" ) {             
            // convert radians to decimal degrees
            point.x *= Proj4js.common.R2D;
            point.y *= Proj4js.common.R2D;
        } else  {               // else project
            dest.forward(point);
            if (dest.to_meter) {
                point.x /= dest.to_meter;
                point.y /= dest.to_meter;
            }
        }
        point.transformed = true;
        return point;
    }, // transform()

    /** datum_transform()
      source coordinate system definition,
      destination coordinate system definition,
      point to transform in geodetic coordinates (long, lat, height)
    */
    datum_transform : function( source, dest, point ) {

      // Short cut if the datums are identical.
      if( source.compare_datums( dest ) ) {
          return point; // in this case, zero is sucess,
                    // whereas cs_compare_datums returns 1 to indicate TRUE
                    // confusing, should fix this
      }
      
      // Explicitly skip datum transform by setting 'datum=none' as parameter for either source or dest
      if( source.datum_type == Proj4js.common.PJD_NODATUM  
          || dest.datum_type == Proj4js.common.PJD_NODATUM) {
          return point; 
      }
      
        // If this datum requires grid shifts, then apply it to geodetic coordinates.
        if( source.datum_type == Proj4js.common.PJD_GRIDSHIFT )
        {
          alert("ERROR: Grid shift transformations are not implemented yet.");
          /*
            pj_apply_gridshift( pj_param(source.params,"snadgrids").s, 0,
                                point_count, point_offset, x, y, z );
            CHECK_RETURN;

            src_a = SRS_WGS84_SEMIMAJOR;
            src_es = 0.006694379990;
          */
        }

        if( dest.datum_type == Proj4js.common.PJD_GRIDSHIFT )
        {
          alert("ERROR: Grid shift transformations are not implemented yet.");
          /*
            dst_a = ;
            dst_es = 0.006694379990;
          */
        }

        // Do we need to go through geocentric coordinates?
        if( source.es != dest.es || source.a != dest.a 
            || source.datum_type == Proj4js.common.PJD_3PARAM 
            || source.datum_type == Proj4js.common.PJD_7PARAM
            || dest.datum_type == Proj4js.common.PJD_3PARAM
            || dest.datum_type == Proj4js.common.PJD_7PARAM)
        {

          // Convert to geocentric coordinates.
          source.geodetic_to_geocentric( point );
          // CHECK_RETURN;

          // Convert between datums
          if( source.datum_type == Proj4js.common.PJD_3PARAM || source.datum_type == Proj4js.common.PJD_7PARAM ) {
            source.geocentric_to_wgs84(point);
            // CHECK_RETURN;
          }

          if( dest.datum_type == Proj4js.common.PJD_3PARAM || dest.datum_type == Proj4js.common.PJD_7PARAM ) {
            dest.geocentric_from_wgs84(point);
            // CHECK_RETURN;
          }

          // Convert back to geodetic coordinates
          dest.geocentric_to_geodetic( point );
            // CHECK_RETURN;
        }

      // Apply grid shift to destination if required
      if( dest.datum_type == Proj4js.common.PJD_GRIDSHIFT )
      {
        alert("ERROR: Grid shift transformations are not implemented yet.");
        // pj_apply_gridshift( pj_param(dest.params,"snadgrids").s, 1, point);
        // CHECK_RETURN;
      }
      return point;
    }, // cs_datum_transform

    /**
     * Function: reportError
     * An internal method to report errors back to user. Should be overridden
     * by applications to deliver error messages.
     */
    reportError: function(msg) {
    },

    /**
     * Function: log
     * An internal method to log events. 
     */
    log: function(msg) {
    },

    loadProjDefinition : function(proj) {

      //check in memory
      if (this.defs[proj.srsCode]) return this.defs[proj.srsCode];

      //set AJAX options
      var options = {
        method: 'get',
        asynchronous: false,          //need to wait until defs are loaded before proceeding
        onSuccess: this.defsLoadedFromDisk.bind(this,proj.srsCode)
      }
      
      //else check for def on the server
      var url = this.libPath + 'defs/' + proj.srsAuth.toUpperCase() + proj.srsProjNumber + '.js';
      new OpenLayers.Ajax.Request(url, options);
      if ( this.defs[proj.srsCode] ) return this.defs[proj.srsCode];

      //else load from web service via AJAX request
      if (this.proxyScript) {
        var url = this.proxyScript + this.defsLookupService +'/' + proj.srsAuth +'/'+ proj.srsProjNumber + '/proj4';
        options.onSuccess = this.defsLoadedFromService.bind(this,proj.srsCode)
        options.onFailure = this.defsFailed.bind(this,proj.srsCode);
        new OpenLayers.Ajax.Request(url, options);
      }
      
      //may return null here if the defs are not found
      return this.defs[proj.srsCode];
    },

    defsLoadedFromDisk: function(srsCode, transport) {
      eval(transport.responseText);
    },

    defsLoadedFromService: function(srsCode, transport) {
      this.defs[srsCode] = transport.responseText;
      // save this also in the prototype, so we don't need to fetch it again
      Proj4js.defs[srsCode] = transport.responseText;
    },

    defsFailed: function(srsCode) {
      this.reportError('failed to load projection definition for: '+srsCode);
      OpenLayers.Util.extend(this.defs[srsCode], this.defs['WGS84']);  //set it to something so it can at least continue
    },

    loadProjCode : function(projName) {
      if (this.Proj[projName]) return;

      //set AJAX options
      var options = {
        method: 'get',
        asynchronous: false,          //need to wait until defs are loaded before proceeding
        onSuccess: this.loadProjCodeSuccess.bind(this, projName),
        onFailure: this.loadProjCodeFailure.bind(this, projName)
      };
      
      //load the projection class 
      var url = this.libPath + 'projCode/' + projName + '.js';
      new OpenLayers.Ajax.Request(url, options);
    },

    loadProjCodeSuccess : function(projName, transport) {
      eval(transport.responseText);
      if (this.Proj[projName].dependsOn){
        this.loadProjCode(this.Proj[projName].dependsOn);
      }
    },

    loadProjCodeFailure : function(projName) {
      Proj4js.reportError("failed to find projection file for: " + projName);
      //TBD initialize with identity transforms so proj will still work
    }

};

/**
 * Class: Proj4js.Proj
 * Projection objects provide coordinate transformation methods for point coordinates
 * once they have been initialized with a projection code.
 */
Proj4js.Proj = OpenLayers.Class({

  /**
   * Property: readyToUse
   * Flag to indicate if initialization is complete for this Proj object
   */
  readyToUse : false,   
  
  /**
   * Property: title
   * The title to describe the projection
   */
  title: null,  
  
  /**
   * Property: projName
   * The projection class for this projection, e.g. lcc (lambert conformal conic,
   * or merc for mercator.  These are exactly equicvalent to their Proj4 
   * counterparts.
   */
  projName: null,
  /**
   * Property: units
   * The units of the projection.  Values include 'm' and 'degrees'
   */
  units: null,
  /**
   * Property: datum
   * The datum specified for the projection
   */
  datum: null,

  /**
   * Constructor: initialize
   * Constructor for Proj4js.Proj objects
  *
  * Parameters:
  * srsCode - a code for map projection definition parameters.  These are usually
  * (but not always) EPSG codes.
  */
  initialize : function(srsCode) {
      this.srsCode = srsCode.toUpperCase();
      if (this.srsCode.indexOf("EPSG") == 0) {
          this.srsCode = this.srsCode;
          this.srsAuth = 'epsg';
          this.srsProjNumber = this.srsCode.substring(5);
      } else {
          this.srsAuth = '';
          this.srsProjNumber = this.srsCode;
      }

      var defs = Proj4js.loadProjDefinition(this);
      if (defs) {
          this.parseDefs(defs);
          Proj4js.loadProjCode(this.projName);
          this.callInit();
      }

  },

  callInit : function() {
      Proj4js.log('projection script loaded for:' + this.projName);
      OpenLayers.Util.extend(this, Proj4js.Proj[this.projName]);
      this.init();
      this.mapXYToLonLat = this.inverse;
      this.lonLatToMapXY = this.forward;
      this.readyToUse = true;
  },

  parseDefs : function(proj4opts) {
      this.defData = proj4opts;
      var paramName, paramVal;
      var paramArray=this.defData.split("+");

      for (var prop=0; prop<paramArray.length; prop++) {
          var property = paramArray[prop].split("=");
          paramName = property[0].toLowerCase();
          paramVal = property[1];

          switch (paramName.replace(/\s/gi,"")) {  // trim out spaces
              case "": break;   // throw away nameless parameter
              case "title":  this.title = paramVal; break;
              case "proj":   this.projName =  paramVal.replace(/\s/gi,""); break;
              case "units":  this.units = paramVal.replace(/\s/gi,""); break;
              case "datum":  this.datumCode = paramVal.replace(/\s/gi,""); break;
              case "nadgrids": this.nagrids = paramVal.replace(/\s/gi,""); break;
              case "ellps":  this.ellps = paramVal.replace(/\s/gi,""); break;
              case "a":      this.a =  parseFloat(paramVal); break;  // semi-major radius
              case "b":      this.b =  parseFloat(paramVal); break;  // semi-minor radius
              case "lat_0":  this.lat0 = paramVal*Proj4js.common.D2R; break;        // phi0, central latitude
              case "lat_1":  this.lat1 = paramVal*Proj4js.common.D2R; break;        //standard parallel 1
              case "lat_2":  this.lat2 = paramVal*Proj4js.common.D2R; break;        //standard parallel 2
              case "lat_ts": this.lat_ts = paramVal*Proj4js.common.D2R; break;      //used in merc 
              case "lon_0":  this.long0 = paramVal*Proj4js.common.D2R; break;       // lam0, central longitude
              case "x_0":    this.x0 = parseFloat(paramVal); break;  // false easting
              case "y_0":    this.y0 = parseFloat(paramVal); break;  // false northing
              case "k_0":    this.k0 = parseFloat(paramVal); break;  // projection scale factor
              case "k":      this.k0 = parseFloat(paramVal); break;  // both forms returned
              case "R_A":    this.R = parseFloat(paramVal); break;   //Spheroid radius 
              case "zone":   this.zone = parseInt(paramVal); break;  // UTM Zone
              case "south":   this.utmSouth = true; break;  // UTM north/south
              case "towgs84":this.datum_params = paramVal.split(","); break;
              case "to_meter": this.to_meter = parseFloat(paramVal); break; // cartesian scaling
              case "from_greenwich": this.from_greenwich = paramVal*Proj4js.common.D2R; break;
              case "pm":     paramVal = paramVal.replace(/\s/gi,"");
                             this.from_greenwich = Proj4js.PrimeMeridian[paramVal] ?
                                Proj4js.PrimeMeridian[paramVal]*Proj4js.common.D2R : 0.0; break;
              case "no_defs": break; 
              default: Proj4js.log("Unrecognized parameter: " + paramName);
          } // switch()
      } // for paramArray
      this.deriveConstants();
  },

  deriveConstants : function() {
      if (this.nagrids == '@null') this.datumCode = 'none';
      if (this.datumCode && this.datumCode != 'none') {
        var datumDef = Proj4js.Datum[this.datumCode];
        if (datumDef) {
          this.datum_params = datumDef.towgs84.split(',');
          this.ellps = datumDef.ellipse;
          this.datumName = datumDef.datumName;
        }
      }
      if (!this.a) {    // do we have an ellipsoid?
          var ellipse = Proj4js.Ellipsoid[this.ellps] ? Proj4js.Ellipsoid[this.ellps] : Proj4js.Ellipsoid['WGS84'];
          OpenLayers.Util.extend(this, ellipse);
      }
      if (this.rf && !this.b) this.b = (1.0 - 1.0/this.rf) * this.a;
      if (Math.abs(this.a - this.b)<Proj4js.common.EPSLN) this.sphere = true;
      this.a2 = this.a * this.a;          // used in geocentric
      this.b2 = this.b * this.b;          // used in geocentric
      this.es = (this.a2-this.b2)/this.a2;  // e ^ 2
      //this.es=1-(Math.pow(this.b,2)/Math.pow(this.a,2));
      this.e = Math.sqrt(this.es);        // eccentricity
      this.ep2=(this.a2-this.b2)/this.b2; // used in geocentric
      if (!this.k0) this.k0 = 1.0;    //default value

      this.datum = new Proj4js.datum(this);
  }
});

Proj4js.Proj.longlat = {
  init : function() {
    //no-op for longlat
  },
  forward : function(pt) {
    //identity transform
    return pt;
  },
  inverse : function(pt) {
    //identity transform
    return pt;
  }
};

/**
  Proj4js.defs is a collection of coordinate system definition objects in the 
  Proj4 command line format.
  Generally a def is added by means of a separate .js file for example:

    <SCRIPT type="text/javascript" src="defs/EPSG26912.js"></SCRIPT>

  def is a CS definition in PROJ.4 WKT format, for example:
    +proj="tmerc"   //longlat, etc.
    +a=majorRadius
    +b=minorRadius
    +lat0=somenumber
    +long=somenumber
*/
Proj4js.defs = {
  // These are so widely used, we'll go ahead and throw them in
  // without requiring a separate .js file
  'WGS84': "+title=long/lat:WGS84 +proj=longlat +ellps=WGS84 +datum=WGS84",
  'EPSG:4326': "+title=long/lat:WGS84 +proj=longlat +a=6378137.0 +b=6356752.31424518 +ellps=WGS84 +datum=WGS84",
  'EPSG:4269': "+title=long/lat:NAD83 +proj=longlat +a=6378137.0 +b=6356752.31414036 +ellps=GRS80 +datum=NAD83" 
};
//+a=6378137.0 +b=6356752.31424518 +ellps=WGS84 +datum=WGS84",
Proj4js.common = {
  PI : Math.PI,
  HALF_PI : Math.PI*0.5,
  TWO_PI : Math.PI*2,
  FORTPI : 0.78539816339744833,
  R2D : 57.2957795131,
  D2R : 0.0174532925199,
  SEC_TO_RAD : 4.84813681109535993589914102357e-6, /* SEC_TO_RAD = Pi/180/3600 */
  EPSLN : 1.0e-10,
  MAX_ITER : 20,
  // following constants from geocent.c
  COS_67P5 : 0.38268343236508977,  /* cosine of 67.5 degrees */
  AD_C : 1.0026000,                /* Toms region 1 constant */

  /* datum_type values */
  PJD_UNKNOWN  : 0,
  PJD_3PARAM   : 1,
  PJD_7PARAM   : 2,
  PJD_GRIDSHIFT: 3,
  PJD_WGS84    : 4,   // WGS84 or equivalent
  PJD_NODATUM  : 5,   // WGS84 or equivalent
  SRS_WGS84_SEMIMAJOR : 6378137.0,  // only used in grid shift transforms

// Function to compute the constant small m which is the radius of
//   a parallel of latitude, phi, divided by the semimajor axis.
// -----------------------------------------------------------------
  msfnz : function(eccent, sinphi, cosphi) {
      var con = eccent * sinphi;
      return cosphi/(Math.sqrt(1.0 - con * con));
  },

// Function to compute the constant small t for use in the forward
//   computations in the Lambert Conformal Conic and the Polar
//   Stereographic projections.
// -----------------------------------------------------------------
  tsfnz : function(eccent, phi, sinphi) {
    var con = eccent * sinphi;
    var com = .5 * eccent;
    con = Math.pow(((1.0 - con) / (1.0 + con)), com);
    return (Math.tan(.5 * (this.HALF_PI - phi))/con);
  },

// Function to compute the latitude angle, phi2, for the inverse of the
//   Lambert Conformal Conic and Polar Stereographic projections.
// ----------------------------------------------------------------
  phi2z : function(eccent, ts) {
    var eccnth = .5 * eccent;
    var con, dphi;
    var phi = this.HALF_PI - 2 * Math.atan(ts);
    for (i = 0; i <= 15; i++) {
      con = eccent * Math.sin(phi);
      dphi = this.HALF_PI - 2 * Math.atan(ts *(Math.pow(((1.0 - con)/(1.0 + con)),eccnth))) - phi;
      phi += dphi;
      if (Math.abs(dphi) <= .0000000001) return phi;
    }
    alert("phi2z has NoConvergence");
    return (-9999);
  },

/* Function to compute constant small q which is the radius of a 
   parallel of latitude, phi, divided by the semimajor axis. 
------------------------------------------------------------*/
  qsfnz : function(eccent,sinphi,cosphi) {
    var con;
    if (eccent > 1.0e-7) {
      con = eccent * sinphi;
      return (( 1.0- eccent * eccent) * (sinphi /(1.0 - con * con) - (.5/eccent)*Math.log((1.0 - con)/(1.0 + con))));
    } else {
      return(2.0 * sinphi);
    }
  },

/* Function to eliminate roundoff errors in asin
----------------------------------------------*/
  asinz : function(x) {
    if (Math.abs(x)>1.0) {
      x=(x>1.0)?1.0:-1.0;
    }
    return Math.asin(x);
  },

// following functions from gctpc cproj.c for transverse mercator projections
  e0fn : function(x) {return(1.0-0.25*x*(1.0+x/16.0*(3.0+1.25*x)));},
  e1fn : function(x) {return(0.375*x*(1.0+0.25*x*(1.0+0.46875*x)));},
  e2fn : function(x) {return(0.05859375*x*x*(1.0+0.75*x));},
  e3fn : function(x) {return(x*x*x*(35.0/3072.0));},
  mlfn : function(e0,e1,e2,e3,phi) {return(e0*phi-e1*Math.sin(2.0*phi)+e2*Math.sin(4.0*phi)-e3*Math.sin(6.0*phi));},

  srat : function(esinp, exp) {
    return(Math.pow((1.0-esinp)/(1.0+esinp), exp));
  },

// Function to return the sign of an argument
  sign : function(x) { if (x < 0.0) return(-1); else return(1);},

// Function to adjust longitude to -180 to 180; input in radians
  adjust_lon : function(x) {
    x = (Math.abs(x) < this.PI) ? x: (x - (this.sign(x)*this.TWO_PI) );
    return x;
  }

};

/** datum object
*/
Proj4js.datum = OpenLayers.Class({

  initialize : function(proj) {
    this.datum_type = Proj4js.common.PJD_WGS84;   //default setting
    if (proj.datumCode && proj.datumCode == 'none') {
      this.datum_type = Proj4js.common.PJD_NODATUM;
    }
    if (proj && proj.datum_params) {
      for (var i=0; i<proj.datum_params.length; i++) {
        proj.datum_params[i]=parseFloat(proj.datum_params[i]);
      }
      if (proj.datum_params[0] != 0 || proj.datum_params[1] != 0 || proj.datum_params[2] != 0 ) {
        this.datum_type = Proj4js.common.PJD_3PARAM;
      }
      if (proj.datum_params.length > 3) {
        if (proj.datum_params[3] != 0 || proj.datum_params[4] != 0 ||
            proj.datum_params[5] != 0 || proj.datum_params[6] != 0 ) {
          this.datum_type = Proj4js.common.PJD_7PARAM;
          proj.datum_params[3] *= Proj4js.common.SEC_TO_RAD;
          proj.datum_params[4] *= Proj4js.common.SEC_TO_RAD;
          proj.datum_params[5] *= Proj4js.common.SEC_TO_RAD;
          proj.datum_params[6] = (proj.datum_params[6]/1000000.0) + 1.0;
        }
      }
    }
    if (proj) {
      this.a = proj.a;    //datum object also uses these values
      this.b = proj.b;
      this.es = proj.es;
      this.ep2 = proj.ep2;
      this.datum_params = proj.datum_params;
    }
  },

  /****************************************************************/
  // cs_compare_datums()
  //   Returns 1 (TRUE) if the two datums match, otherwise 0 (FALSE).
  compare_datums : function( dest ) {
    if( this.datum_type != dest.datum_type ) {
      return false; // false, datums are not equal
    } else if (this.a != dest.a || Math.abs(this.es-dest.es) > 0.000000000050) {
      // the tolerence for es is to ensure that GRS80 and WGS84
      // are considered identical
      return false;
    } else if( this.datum_type == Proj4js.common.PJD_3PARAM ) {
      return (this.datum_params[0] == dest.datum_params[0]
              && this.datum_params[1] == dest.datum_params[1]
              && this.datum_params[2] == dest.datum_params[2]);
    } else if( this.datum_type == Proj4js.common.PJD_7PARAM ) {
      return (this.datum_params[0] == dest.datum_params[0]
              && this.datum_params[1] == dest.datum_params[1]
              && this.datum_params[2] == dest.datum_params[2]
              && this.datum_params[3] == dest.datum_params[3]
              && this.datum_params[4] == dest.datum_params[4]
              && this.datum_params[5] == dest.datum_params[5]
              && this.datum_params[6] == dest.datum_params[6]);
    } else if( this.datum_type == Proj4js.common.PJD_GRIDSHIFT ) {
      return strcmp( pj_param(this.params,"snadgrids").s,
                     pj_param(dest.params,"snadgrids").s ) == 0;
    } else {
      return true; // datums are equal
    }
  }, // cs_compare_datums()

  /*
   * The function Convert_Geodetic_To_Geocentric converts geodetic coordinates
   * (latitude, longitude, and height) to geocentric coordinates (X, Y, Z),
   * according to the current ellipsoid parameters.
   *
   *    Latitude  : Geodetic latitude in radians                     (input)
   *    Longitude : Geodetic longitude in radians                    (input)
   *    Height    : Geodetic height, in meters                       (input)
   *    X         : Calculated Geocentric X coordinate, in meters    (output)
   *    Y         : Calculated Geocentric Y coordinate, in meters    (output)
   *    Z         : Calculated Geocentric Z coordinate, in meters    (output)
   *
   */
  geodetic_to_geocentric : function(p) {
    var Longitude = p.x;
    var Latitude = p.y;
    var Height = p.z ? p.z : 0;   //Z value not always supplied
    var X;  // output
    var Y;
    var Z;

    var Error_Code=0;  //  GEOCENT_NO_ERROR;
    var Rn;            /*  Earth radius at location  */
    var Sin_Lat;       /*  Math.sin(Latitude)  */
    var Sin2_Lat;      /*  Square of Math.sin(Latitude)  */
    var Cos_Lat;       /*  Math.cos(Latitude)  */

    /*
    ** Don't blow up if Latitude is just a little out of the value
    ** range as it may just be a rounding issue.  Also removed longitude
    ** test, it should be wrapped by Math.cos() and Math.sin().  NFW for PROJ.4, Sep/2001.
    */
    if (Latitude < -Proj4js.common.HALF_PI && Latitude > -1.001 * Proj4js.common.HALF_PI ) {
        Latitude = -Proj4js.common.HALF_PI;
    } else if (Latitude > Proj4js.common.HALF_PI && Latitude < 1.001 * Proj4js.common.HALF_PI ) {
        Latitude = Proj4js.common.HALF_PI;
    } else if ((Latitude < -Proj4js.common.HALF_PI) || (Latitude > Proj4js.common.HALF_PI)) {
      /* Latitude out of range */
      Proj4js.reportError('geocent:lat out of range:'+Latitude);
      return null;
    }

    if (Longitude > Proj4js.common.PI) Longitude -= (2*Proj4js.common.PI);
    Sin_Lat = Math.sin(Latitude);
    Cos_Lat = Math.cos(Latitude);
    Sin2_Lat = Sin_Lat * Sin_Lat;
    Rn = this.a / (Math.sqrt(1.0e0 - this.es * Sin2_Lat));
    X = (Rn + Height) * Cos_Lat * Math.cos(Longitude);
    Y = (Rn + Height) * Cos_Lat * Math.sin(Longitude);
    Z = ((Rn * (1 - this.es)) + Height) * Sin_Lat;

    p.x = X;
    p.y = Y;
    p.z = Z;
    return Error_Code;
  }, // cs_geodetic_to_geocentric()

  geocentric_to_geodetic : function (p) {
/* local defintions and variables */
/* end-criterium of loop, accuracy of sin(Latitude) */
var genau = 1.E-12;
var genau2 = (genau*genau);
var maxiter = 30;

    var P;        /* distance between semi-minor axis and location */
    var RR;       /* distance between center and location */
    var CT;       /* sin of geocentric latitude */
    var ST;       /* cos of geocentric latitude */
    var RX;
    var RK;
    var RN;       /* Earth radius at location */
    var CPHI0;    /* cos of start or old geodetic latitude in iterations */
    var SPHI0;    /* sin of start or old geodetic latitude in iterations */
    var CPHI;     /* cos of searched geodetic latitude */
    var SPHI;     /* sin of searched geodetic latitude */
    var SDPHI;    /* end-criterium: addition-theorem of sin(Latitude(iter)-Latitude(iter-1)) */
    var At_Pole;     /* indicates location is in polar region */
    var iter;        /* # of continous iteration, max. 30 is always enough (s.a.) */

    var X =p.x;
    var Y = p.y;
    var Z = p.z ? p.z : 0.0;   //Z value not always supplied
    var Longitude;
    var Latitude;
    var Height;
    
    At_Pole = false;
    P = Math.sqrt(X*X+Y*Y);
    RR = Math.sqrt(X*X+Y*Y+Z*Z);

/*  special cases for latitude and longitude */
    if (P/this.a < genau) {

/*  special case, if P=0. (X=0., Y=0.) */
        At_Pole = true;
        Longitude = 0.0;

/*  if (X,Y,Z)=(0.,0.,0.) then Height becomes semi-minor axis
 *  of ellipsoid (=center of mass), Latitude becomes PI/2 */
        if (RR/this.a < genau) {
            Latitude = Proj4js.common.HALF_PI;
            Height   = -this.b;
            return;
        }
    } else {
/*  ellipsoidal (geodetic) longitude
 *  interval: -PI < Longitude <= +PI */
        Longitude=Math.atan2(Y,X);
    }

/* --------------------------------------------------------------
 * Following iterative algorithm was developped by
 * "Institut f�r Erdmessung", University of Hannover, July 1988.
 * Internet: www.ife.uni-hannover.de
 * Iterative computation of CPHI,SPHI and Height.
 * Iteration of CPHI and SPHI to 10**-12 radian resp.
 * 2*10**-7 arcsec.
 * --------------------------------------------------------------
 */
    CT = Z/RR;
    ST = P/RR;
    RX = 1.0/Math.sqrt(1.0-this.es*(2.0-this.es)*ST*ST);
    CPHI0 = ST*(1.0-this.es)*RX;
    SPHI0 = CT*RX;
    iter = 0;

/* loop to find sin(Latitude) resp. Latitude
 * until |sin(Latitude(iter)-Latitude(iter-1))| < genau */
    do
    {
        iter++;
        RN = this.a/Math.sqrt(1.0-this.es*SPHI0*SPHI0);

/*  ellipsoidal (geodetic) height */
        Height = P*CPHI0+Z*SPHI0-RN*(1.0-this.es*SPHI0*SPHI0);

        RK = this.es*RN/(RN+Height);
        RX = 1.0/Math.sqrt(1.0-RK*(2.0-RK)*ST*ST);
        CPHI = ST*(1.0-RK)*RX;
        SPHI = CT*RX;
        SDPHI = SPHI*CPHI0-CPHI*SPHI0;
        CPHI0 = CPHI;
        SPHI0 = SPHI;
    }
    while (SDPHI*SDPHI > genau2 && iter < maxiter);

/*  ellipsoidal (geodetic) latitude */
    Latitude=Math.atan(SPHI/Math.abs(CPHI));

    p.x = Longitude;
    p.y =Latitude;
    p.z = Height;
    return p;
  },

  /** Convert_Geocentric_To_Geodetic
   * The method used here is derived from 'An Improved Algorithm for
   * Geocentric to Geodetic Coordinate Conversion', by Ralph Toms, Feb 1996
   */
  geocentric_to_geodetic_noniter : function (p) {
    var X =p.x;
    var Y = p.y;
    var Z = p.z ? p.z : 0;   //Z value not always supplied
    var Longitude;
    var Latitude;
    var Height;

    var W;        /* distance from Z axis */
    var W2;       /* square of distance from Z axis */
    var T0;       /* initial estimate of vertical component */
    var T1;       /* corrected estimate of vertical component */
    var S0;       /* initial estimate of horizontal component */
    var S1;       /* corrected estimate of horizontal component */
    var Sin_B0;   /* Math.sin(B0), B0 is estimate of Bowring aux variable */
    var Sin3_B0;  /* cube of Math.sin(B0) */
    var Cos_B0;   /* Math.cos(B0) */
    var Sin_p1;   /* Math.sin(phi1), phi1 is estimated latitude */
    var Cos_p1;   /* Math.cos(phi1) */
    var Rn;       /* Earth radius at location */
    var Sum;      /* numerator of Math.cos(phi1) */
    var At_Pole;  /* indicates location is in polar region */

    X = parseFloat(X);  // cast from string to float
    Y = parseFloat(Y);
    Z = parseFloat(Z);

    At_Pole = false;
    if (X != 0.0)
    {
        Longitude = Math.atan2(Y,X);
    }
    else
    {
        if (Y > 0)
        {
            Longitude = Proj4js.common.HALF_PI;
        }
        else if (Y < 0)
        {
            Longitude = -Proj4js.common.HALF_PI;
        }
        else
        {
            At_Pole = true;
            Longitude = 0.0;
            if (Z > 0.0)
            {  /* north pole */
                Latitude = Proj4js.common.HALF_PI;
            }
            else if (Z < 0.0)
            {  /* south pole */
                Latitude = -Proj4js.common.HALF_PI;
            }
            else
            {  /* center of earth */
                Latitude = Proj4js.common.HALF_PI;
                Height = -this.b;
                return;
            }
        }
    }
    W2 = X*X + Y*Y;
    W = Math.sqrt(W2);
    T0 = Z * Proj4js.common.AD_C;
    S0 = Math.sqrt(T0 * T0 + W2);
    Sin_B0 = T0 / S0;
    Cos_B0 = W / S0;
    Sin3_B0 = Sin_B0 * Sin_B0 * Sin_B0;
    T1 = Z + this.b * this.ep2 * Sin3_B0;
    Sum = W - this.a * this.es * Cos_B0 * Cos_B0 * Cos_B0;
    S1 = Math.sqrt(T1*T1 + Sum * Sum);
    Sin_p1 = T1 / S1;
    Cos_p1 = Sum / S1;
    Rn = this.a / Math.sqrt(1.0 - this.es * Sin_p1 * Sin_p1);
    if (Cos_p1 >= Proj4js.common.COS_67P5)
    {
        Height = W / Cos_p1 - Rn;
    }
    else if (Cos_p1 <= -Proj4js.common.COS_67P5)
    {
        Height = W / -Cos_p1 - Rn;
    }
    else
    {
        Height = Z / Sin_p1 + Rn * (this.es - 1.0);
    }
    if (At_Pole == false)
    {
        Latitude = Math.atan(Sin_p1 / Cos_p1);
    }

    p.x = Longitude;
    p.y =Latitude;
    p.z = Height;
    return p;
  }, // cs_geocentric_to_geodetic()

  /****************************************************************/
  // pj_geocentic_to_wgs84( p )
  //  p = point to transform in geocentric coordinates (x,y,z)
  geocentric_to_wgs84 : function ( p ) {

    if( this.datum_type == Proj4js.common.PJD_3PARAM )
    {
      // if( x[io] == HUGE_VAL )
      //    continue;
      p.x += this.datum_params[0];
      p.y += this.datum_params[1];
      p.z += this.datum_params[2];

    }
    else  // if( this.datum_type == Proj4js.common.PJD_7PARAM )
    {
      var Dx_BF =this.datum_params[0];
      var Dy_BF =this.datum_params[1];
      var Dz_BF =this.datum_params[2];
      var Rx_BF =this.datum_params[3];
      var Ry_BF =this.datum_params[4];
      var Rz_BF =this.datum_params[5];
      var M_BF  =this.datum_params[6];
      // if( x[io] == HUGE_VAL )
      //    continue;
      var x_out = M_BF*(       p.x - Rz_BF*p.y + Ry_BF*p.z) + Dx_BF;
      var y_out = M_BF*( Rz_BF*p.x +       p.y - Rx_BF*p.z) + Dy_BF;
      var z_out = M_BF*(-Ry_BF*p.x + Rx_BF*p.y +       p.z) + Dz_BF;
      p.x = x_out;
      p.y = y_out;
      p.z = z_out;
    }
  }, // cs_geocentric_to_wgs84

  /****************************************************************/
  // pj_geocentic_from_wgs84()
  //  coordinate system definition,
  //  point to transform in geocentric coordinates (x,y,z)
  geocentric_from_wgs84 : function( p ) {

    if( this.datum_type == Proj4js.common.PJD_3PARAM )
    {
      //if( x[io] == HUGE_VAL )
      //    continue;
      p.x -= this.datum_params[0];
      p.y -= this.datum_params[1];
      p.z -= this.datum_params[2];

    }
    else // if( this.datum_type == Proj4js.common.PJD_7PARAM )
    {
      var Dx_BF =this.datum_params[0];
      var Dy_BF =this.datum_params[1];
      var Dz_BF =this.datum_params[2];
      var Rx_BF =this.datum_params[3];
      var Ry_BF =this.datum_params[4];
      var Rz_BF =this.datum_params[5];
      var M_BF  =this.datum_params[6];
      var x_tmp = (p.x - Dx_BF) / M_BF;
      var y_tmp = (p.y - Dy_BF) / M_BF;
      var z_tmp = (p.z - Dz_BF) / M_BF;
      //if( x[io] == HUGE_VAL )
      //    continue;

      p.x =        x_tmp + Rz_BF*y_tmp - Ry_BF*z_tmp;
      p.y = -Rz_BF*x_tmp +       y_tmp + Rx_BF*z_tmp;
      p.z =  Ry_BF*x_tmp - Rx_BF*y_tmp +       z_tmp;
    } //cs_geocentric_from_wgs84()
  }
});

/** point object, nothing fancy, just allows values to be
    passed back and forth by reference rather than by value.
    Other point classes may be used as long as they have
    x and y properties, which will get modified in the transform method.
*/
Proj4js.Point = OpenLayers.Class({

    initialize : function(x,y,z) {
      if (typeof x == 'object') {
        this.x = x[0];
        this.y = x[1];
        this.z = x[2] || 0.0;
      } else {
        this.x = x;
        this.y = y;
        this.z = z || 0.0;
      }
    },

    clone : function() {
      return new Proj4js.Point(this.x, this.y, this.z);
    },

    /**
     * Method: toString
     * Return a readable string version of the lonlat
     *
     * Return:
     * {String} String representation of Proj4js.Point object. 
     *           (ex. <i>"x=5,y=42"</i>)
     */
    toString:function() {
        return ("x=" + this.x + ",y=" + this.y);
    },

    /** 
     * APIMethod: toShortString
     * 
     * Return:
     * {String} Shortened String representation of Proj4js.Point object. 
     *         (ex. <i>"5, 42"</i>)
     */
    toShortString:function() {
        return (this.x + ", " + this.y);
    }
});

Proj4js.PrimeMeridian = {
    "greenwich": 0.0,               //"0dE",
    "lisbon":     -9.131906111111,   //"9d07'54.862\"W",
    "paris":       2.337229166667,   //"2d20'14.025\"E",
    "bogota":    -74.080916666667,  //"74d04'51.3\"W",
    "madrid":     -3.687938888889,  //"3d41'16.58\"W",
    "rome":       12.452333333333,  //"12d27'8.4\"E",
    "bern":        7.439583333333,  //"7d26'22.5\"E",
    "jakarta":   106.807719444444,  //"106d48'27.79\"E",
    "ferro":     -17.666666666667,  //"17d40'W",
    "brussels":    4.367975,        //"4d22'4.71\"E",
    "stockholm":  18.058277777778,  //"18d3'29.8\"E",
    "athens":     23.7163375,       //"23d42'58.815\"E",
    "oslo":       10.722916666667   //"10d43'22.5\"E"
};

Proj4js.Ellipsoid = {
  "MERIT": {a:6378137.0, rf:298.257, ellipseName:"MERIT 1983"},
  "SGS85": {a:6378136.0, rf:298.257, ellipseName:"Soviet Geodetic System 85"},
  "GRS80": {a:6378137.0, rf:298.257222101, ellipseName:"GRS 1980(IUGG, 1980)"},
  "IAU76": {a:6378140.0, rf:298.257, ellipseName:"IAU 1976"},
  "airy": {a:6377563.396, b:6356256.910, ellipseName:"Airy 1830"},
  "APL4.": {a:6378137, rf:298.25, ellipseName:"Appl. Physics. 1965"},
  "NWL9D": {a:6378145.0, rf:298.25, ellipseName:"Naval Weapons Lab., 1965"},
  "mod_airy": {a:6377340.189, b:6356034.446, ellipseName:"Modified Airy"},
  "andrae": {a:6377104.43, rf:300.0, ellipseName:"Andrae 1876 (Den., Iclnd.)"},
  "aust_SA": {a:6378160.0, rf:298.25, ellipseName:"Australian Natl & S. Amer. 1969"},
  "GRS67": {a:6378160.0, rf:298.2471674270, ellipseName:"GRS 67(IUGG 1967)"},
  "bessel": {a:6377397.155, rf:299.1528128, ellipseName:"Bessel 1841"},
  "bess_nam": {a:6377483.865, rf:299.1528128, ellipseName:"Bessel 1841 (Namibia)"},
  "clrk66": {a:6378206.4, b:6356583.8, ellipseName:"Clarke 1866"},
  "clrk80": {a:6378249.145, rf:293.4663, ellipseName:"Clarke 1880 mod."},
  "CPM": {a:6375738.7, rf:334.29, ellipseName:"Comm. des Poids et Mesures 1799"},
  "delmbr": {a:6376428.0, rf:311.5, ellipseName:"Delambre 1810 (Belgium)"},
  "engelis": {a:6378136.05, rf:298.2566, ellipseName:"Engelis 1985"},
  "evrst30": {a:6377276.345, rf:300.8017, ellipseName:"Everest 1830"},
  "evrst48": {a:6377304.063, rf:300.8017, ellipseName:"Everest 1948"},
  "evrst56": {a:6377301.243, rf:300.8017, ellipseName:"Everest 1956"},
  "evrst69": {a:6377295.664, rf:300.8017, ellipseName:"Everest 1969"},
  "evrstSS": {a:6377298.556, rf:300.8017, ellipseName:"Everest (Sabah & Sarawak)"},
  "fschr60": {a:6378166.0, rf:298.3, ellipseName:"Fischer (Mercury Datum) 1960"},
  "fschr60m": {a:6378155.0, rf:298.3, ellipseName:"Fischer 1960"},
  "fschr68": {a:6378150.0, rf:298.3, ellipseName:"Fischer 1968"},
  "helmert": {a:6378200.0, rf:298.3, ellipseName:"Helmert 1906"},
  "hough": {a:6378270.0, rf:297.0, ellipseName:"Hough"},
  "intl": {a:6378388.0, rf:297.0, ellipseName:"International 1909 (Hayford)"},
  "kaula": {a:6378163.0, rf:298.24, ellipseName:"Kaula 1961"},
  "lerch": {a:6378139.0, rf:298.257, ellipseName:"Lerch 1979"},
  "mprts": {a:6397300.0, rf:191.0, ellipseName:"Maupertius 1738"},
  "new_intl": {a:6378157.5, b:6356772.2, ellipseName:"New International 1967"},
  "plessis": {a:6376523.0, rf:6355863.0, ellipseName:"Plessis 1817 (France)"},
  "krass": {a:6378245.0, rf:298.3, ellipseName:"Krassovsky, 1942"},
  "SEasia": {a:6378155.0, b:6356773.3205, ellipseName:"Southeast Asia"},
  "walbeck": {a:6376896.0, b:6355834.8467, ellipseName:"Walbeck"},
  "WGS60": {a:6378165.0, rf:298.3, ellipseName:"WGS 60"},
  "WGS66": {a:6378145.0, rf:298.25, ellipseName:"WGS 66"},
  "WGS72": {a:6378135.0, rf:298.26, ellipseName:"WGS 72"},
  "WGS84": {a:6378137.0, rf:298.257223563, ellipseName:"WGS 84"},
  "sphere": {a:6370997.0, b:6370997.0, ellipseName:"Normal Sphere (r=6370997)"}
};

Proj4js.Datum = {
  "WGS84": {towgs84: "0,0,0", ellipse: "WGS84", datumName: ""},
  "GGRS87": {towgs84: "-199.87,74.79,246.62", ellipse: "GRS80", datumName: "Greek_Geodetic_Reference_System_1987"},
  "NAD83": {towgs84: "0,0,0", ellipse: "GRS80", datumName: "North_American_Datum_1983"},
  "NAD27": {nadgrids: "@conus,@alaska,@ntv2_0.gsb,@ntv1_can.dat", ellipse: "clrk66", datumName: "North_American_Datum_1927"},
  "potsdam": {towgs84: "606.0,23.0,413.0", ellipse: "bessel", datumName: "Potsdam Rauenberg 1950 DHDN"},
  "carthage": {towgs84: "-263.0,6.0,431.0", ellipse: "clark80", datumName: "Carthage 1934 Tunisia"},
  "hermannskogel": {towgs84: "653.0,-212.0,449.0", ellipse: "bessel", datumName: "Hermannskogel"},
  "ire65": {towgs84: "482.530,-130.596,564.557,-1.042,-0.214,-0.631,8.15", ellipse: "mod_airy", datumName: "Ireland 1965"},
  "nzgd49": {towgs84: "59.47,-5.04,187.44,0.47,-0.1,1.024,-4.5993", ellipse: "intl", datumName: "New Zealand Geodetic Datum 1949"},
  "OSGB36": {towgs84: "446.448,-125.157,542.060,0.1502,0.2470,0.8421,-20.4894", ellipse: "airy", datumName: "Airy 1830"}
};

Proj4js.WGS84 = new Proj4js.Proj('WGS84');
Proj4js.Datum['OSB36'] = Proj4js.Datum['OSGB36']; //as returned from spatialreference.org
/* ======================================================================
    projCode/sterea.js
   ====================================================================== */


Proj4js.Proj.sterea = {
  dependsOn : 'gauss',

  init : function() {
    Proj4js.Proj['gauss'].init.apply(this);
    if (!this.rc) {
      Proj4js.reportError("sterea:init:E_ERROR_0");
      return;
    }
    this.sinc0 = Math.sin(this.phic0);
    this.cosc0 = Math.cos(this.phic0);
    this.R2 = 2.0 * this.rc;
    if (!this.title) this.title = "Oblique Stereographic Alternative";
  },

  forward : function(p) {
    p.x = Proj4js.common.adjust_lon(p.x-this.long0); /* adjust del longitude */
    Proj4js.Proj['gauss'].forward.apply(this, [p]);
    sinc = Math.sin(p.y);
    cosc = Math.cos(p.y);
    cosl = Math.cos(p.x);
    k = this.k0 * this.R2 / (1.0 + this.sinc0 * sinc + this.cosc0 * cosc * cosl);
    p.x = k * cosc * Math.sin(p.x);
    p.y = k * (this.cosc0 * sinc - this.sinc0 * cosc * cosl);
    p.x = this.a * p.x + this.x0;
    p.y = this.a * p.y + this.y0;
    return p;
  },

  inverse : function(p) {
    var lon,lat;
    p.x = (p.x - this.x0) / this.a; /* descale and de-offset */
    p.y = (p.y - this.y0) / this.a;

    p.x /= this.k0;
    p.y /= this.k0;
    if ( (rho = Math.sqrt(p.x*p.x + p.y*p.y)) ) {
      c = 2.0 * Math.atan2(rho, this.R2);
      sinc = Math.sin(c);
      cosc = Math.cos(c);
      lat = Math.asin(cosc * this.sinc0 + p.y * sinc * this.cosc0 / rho);
      lon = Math.atan2(p.x * sinc, rho * this.cosc0 * cosc - p.y * this.sinc0 * sinc);
    } else {
      lat = this.phic0;
      lon = 0.;
    }

    p.x = lon;
    p.y = lat;
    Proj4js.Proj['gauss'].inverse.apply(this,[p]);
    p.x = Proj4js.common.adjust_lon(p.x + this.long0); /* adjust longitude to CM */
    return p;
  }
};

/* ======================================================================
    projCode/aea.js
   ====================================================================== */

/*******************************************************************************
NAME                     ALBERS CONICAL EQUAL AREA 

PURPOSE:  Transforms input longitude and latitude to Easting and Northing
    for the Albers Conical Equal Area projection.  The longitude
    and latitude must be in radians.  The Easting and Northing
    values will be returned in meters.

PROGRAMMER              DATE
----------              ----
T. Mittan,        Feb, 1992

ALGORITHM REFERENCES

1.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

2.  Snyder, John P. and Voxland, Philip M., "An Album of Map Projections",
    U.S. Geological Survey Professional Paper 1453 , United State Government
    Printing Office, Washington D.C., 1989.
*******************************************************************************/


Proj4js.Proj.aea = {
  init : function() {

    if (Math.abs(this.lat1 + this.lat2) < Proj4js.common.EPSLN) {
       Proj4js.reportError("aeaInitEqualLatitudes");
       return;
    }
    this.temp = this.b / this.a;
    this.es = 1.0 - Math.pow(this.temp,2);
    this.e3 = Math.sqrt(this.es);

    this.sin_po=Math.sin(this.lat1);
    this.cos_po=Math.cos(this.lat1);
    this.t1=this.sin_po
    this.con = this.sin_po;
    this.ms1 = Proj4js.common.msfnz(this.e3,this.sin_po,this.cos_po);
    this.qs1 = Proj4js.common.qsfnz(this.e3,this.sin_po,this.cos_po);

    this.sin_po=Math.sin(this.lat2);
    this.cos_po=Math.cos(this.lat2);
    this.t2=this.sin_po;
    this.ms2 = Proj4js.common.msfnz(this.e3,this.sin_po,this.cos_po);
    this.qs2 = Proj4js.common.qsfnz(this.e3,this.sin_po,this.cos_po);

    this.sin_po=Math.sin(this.lat0);
    this.cos_po=Math.cos(this.lat0);
    this.t3=this.sin_po;
    this.qs0 = Proj4js.common.qsfnz(this.e3,this.sin_po,this.cos_po);

    if (Math.abs(this.lat1 - this.lat2) > Proj4js.common.EPSLN) {
      this.ns0 = (this.ms1 * this.ms1 - this.ms2 *this.ms2)/ (this.qs2 - this.qs1);
    } else {
      this.ns0 = this.con;
    }
    this.c = this.ms1 * this.ms1 + this.ns0 * this.qs1;
    this.rh = this.a * Math.sqrt(this.c - this.ns0 * this.qs0)/this.ns0;
  },

/* Albers Conical Equal Area forward equations--mapping lat,long to x,y
  -------------------------------------------------------------------*/
  forward: function(p){

    var lon=p.x;
    var lat=p.y;

    this.sin_phi=Math.sin(lat);
    this.cos_phi=Math.cos(lat);

    var qs = Proj4js.common.qsfnz(this.e3,this.sin_phi,this.cos_phi);
    var rh1 =this.a * Math.sqrt(this.c - this.ns0 * qs)/this.ns0;
    var theta = this.ns0 * Proj4js.common.adjust_lon(lon - this.long0); 
    var x = rh1 * Math.sin(theta) + this.x0;
    var y = this.rh - rh1 * Math.cos(theta) + this.y0;

    p.x = x; 
    p.y = y;
    return p;
  },


  inverse: function(p) {
    var rh1,qs,con,theta,lon,lat;

    p.x -= this.x0;
    p.y = this.rh - p.y + this.y0;
    if (this.ns0 >= 0) {
      rh1 = Math.sqrt(p.x *p.x + p.y * p.y);
      con = 1.0;
    } else {
      rh1 = -Math.sqrt(p.x * p.x + p.y *p.y);
      con = -1.0;
    }
    theta = 0.0;
    if (rh1 != 0.0) {
      theta = Math.atan2(con * p.x, con * p.y);
    }
    con = rh1 * this.ns0 / this.a;
    qs = (this.c - con * con) / this.ns0;
    if (this.e3 >= 1e-10) {
      con = 1 - .5 * (1.0 -this.es) * Math.log((1.0 - this.e3) / (1.0 + this.e3))/this.e3;
      if (Math.abs(Math.abs(con) - Math.abs(qs)) > .0000000001 ) {
          lat = this.phi1z(this.e3,qs);
      } else {
          if (qs >= 0) {
             lat = .5 * PI;
          } else {
             lat = -.5 * PI;
          }
      }
    } else {
      lat = this.phi1z(e3,qs);
    }

    lon = Proj4js.common.adjust_lon(theta/this.ns0 + this.long0);
    p.x = lon;
    p.y = lat;
    return p;
  },
  
/* Function to compute phi1, the latitude for the inverse of the
   Albers Conical Equal-Area projection.
-------------------------------------------*/
  phi1z: function (eccent,qs) {
    var con, com, dphi;
    var phi = Proj4js.common.asinz(.5 * qs);
    if (eccent < Proj4js.common.EPSLN) return phi;
    
    var eccnts = eccent * eccent; 
    for (var i = 1; i <= 25; i++) {
        sinphi = Math.sin(phi);
        cosphi = Math.cos(phi);
        con = eccent * sinphi; 
        com = 1.0 - con * con;
        dphi = .5 * com * com / cosphi * (qs / (1.0 - eccnts) - sinphi / com + .5 / eccent * Math.log((1.0 - con) / (1.0 + con)));
        phi = phi + dphi;
        if (Math.abs(dphi) <= 1e-7) return phi;
    }
    Proj4js.reportError("aea:phi1z:Convergence error");
    return null;
  }
  
};



/* ======================================================================
    projCode/poly.js
   ====================================================================== */

/* Function to compute, phi4, the latitude for the inverse of the
   Polyconic projection.
------------------------------------------------------------*/
function phi4z (eccent,e0,e1,e2,e3,a,b,c,phi) {
  var sinphi, sin2ph, tanph, ml, mlp, con1, con2, con3, dphi, i;

  phi = a;
  for (i = 1; i <= 15; i++) {
    sinphi = Math.sin(phi);
    tanphi = Math.tan(phi);
    c = tanphi * Math.sqrt (1.0 - eccent * sinphi * sinphi);
    sin2ph = Math.sin (2.0 * phi);
    /*
    ml = e0 * *phi - e1 * sin2ph + e2 * sin (4.0 *  *phi);
    mlp = e0 - 2.0 * e1 * cos (2.0 *  *phi) + 4.0 * e2 *  cos (4.0 *  *phi);
    */
    ml = e0 * phi - e1 * sin2ph + e2 * Math.sin (4.0 *  phi) - e3 * Math.sin (6.0 * phi);
    mlp = e0 - 2.0 * e1 * Math.cos (2.0 *  phi) + 4.0 * e2 * Math.cos (4.0 *  phi) - 6.0 * e3 * Math.cos (6.0 *  phi);
    con1 = 2.0 * ml + c * (ml * ml + b) - 2.0 * a *  (c * ml + 1.0);
    con2 = eccent * sin2ph * (ml * ml + b - 2.0 * a * ml) / (2.0 *c);
    con3 = 2.0 * (a - ml) * (c * mlp - 2.0 / sin2ph) - 2.0 * mlp;
    dphi = con1 / (con2 + con3);
    phi += dphi;
    if (Math.abs(dphi) <= .0000000001 ) return(phi);   
  }
  Proj4js.reportError("phi4z: No convergence");
  return null;
}


/* Function to compute the constant e4 from the input of the eccentricity
   of the spheroid, x.  This constant is used in the Polar Stereographic
   projection.
--------------------------------------------------------------------*/
function e4fn(x) {
  var con, com;
  con = 1.0 + x;
  com = 1.0 - x;
  return (Math.sqrt((Math.pow(con,con))*(Math.pow(com,com))));
}





/*******************************************************************************
NAME                             POLYCONIC 

PURPOSE:  Transforms input longitude and latitude to Easting and
    Northing for the Polyconic projection.  The
    longitude and latitude must be in radians.  The Easting
    and Northing values will be returned in meters.

PROGRAMMER              DATE
----------              ----
T. Mittan   Mar, 1993

ALGORITHM REFERENCES

1.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

2.  Snyder, John P. and Voxland, Philip M., "An Album of Map Projections",
    U.S. Geological Survey Professional Paper 1453 , United State Government
    Printing Office, Washington D.C., 1989.
*******************************************************************************/

Proj4js.Proj.poly = {

  /* Initialize the POLYCONIC projection
    ----------------------------------*/
  init: function() {
    var temp;     /* temporary variable   */
    if (this.lat0=0) this.lat0=90;//this.lat0 ca

    /* Place parameters in static storage for common use
      -------------------------------------------------*/
    this.temp = this.b / this.a;
    this.es = 1.0 - Math.pow(this.temp,2);// devait etre dans tmerc.js mais n y est pas donc je commente sinon retour de valeurs nulles 
    this.e = Math.sqrt(this.es);
    this.e0 = Proj4js.common.e0fn(this.es);
    this.e1 = Proj4js.common.e1fn(this.es);
    this.e2 = Proj4js.common.e2fn(this.es);
    this.e3 = Proj4js.common.e3fn(this.es);
    this.ml0 = Proj4js.common.mlfn(this.e0, this.e1,this.e2, this.e3, this.lat0);//si que des zeros le calcul ne se fait pas
    //if (!this.ml0) {this.ml0=0;}
  },


  /* Polyconic forward equations--mapping lat,long to x,y
    ---------------------------------------------------*/
  forward: function(p) {
    var sinphi, cosphi; /* sin and cos value        */
    var al;       /* temporary values       */
    var c;        /* temporary values       */
    var con, ml;    /* cone constant, small m     */
    var ms;       /* small m          */
    var x,y;

    var lon=p.x;
    var lat=p.y;  

    con = Proj4js.common.adjust_lon(lon - this.long0);
    if (Math.abs(lat) <= .0000001) {
      x = this.x0 + this.a * con;
      y = this.y0 - this.a * this.ml0;
    } else {
      sinphi = Math.sin(lat);
      cosphi = Math.cos(lat);    

      ml = Proj4js.common.mlfn(this.e0, this.e1, this.e2, this.e3, lat);
      ms = Proj4js.common.msfnz(this.e,sinphi,cosphi);
      con = sinphi;
      x = this.x0 + this.a * ms * Math.sin(con)/sinphi;
      y = this.y0 + this.a * (ml - this.ml0 + ms * (1.0 - Math.cos(con))/sinphi);
    }

    p.x=x;
    p.y=y;   
    return p;
  },


  /* Inverse equations
  -----------------*/
  inverse: function(p) {
    var sin_phi, cos_phi; /* sin and cos value        */
    var al;         /* temporary values       */
    var b;          /* temporary values       */
    var c;          /* temporary values       */
    var con, ml;      /* cone constant, small m     */
    var iflg;       /* error flag         */
    var lon,lat;
    p.x -= this.x0;
    p.y -= this.y0;
    al = this.ml0 + p.y/this.a;
    iflg = 0;

    if (Math.abs(al) <= .0000001) {
      lon = p.x/this.a + this.long0;
      lat = 0.0;
    } else {
      b = al * al + (p.x/this.a) * (p.x/this.a);
      iflg = phi4z(this.es,this.e0,this.e1,this.e2,this.e3,this.al,b,c,lat);
      if (iflg != 1) return(iflg);
      lon = Proj4js.common.adjust_lon((asinz(p.x * c / this.a) / Math.sin(lat)) + this.long0);
    }

    p.x=lon;
    p.y=lat;
    return p;
  }
};



/* ======================================================================
    projCode/equi.js
   ====================================================================== */

/*******************************************************************************
NAME                             EQUIRECTANGULAR 

PURPOSE:  Transforms input longitude and latitude to Easting and
    Northing for the Equirectangular projection.  The
    longitude and latitude must be in radians.  The Easting
    and Northing values will be returned in meters.

PROGRAMMER              DATE
----------              ----
T. Mittan   Mar, 1993

ALGORITHM REFERENCES

1.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

2.  Snyder, John P. and Voxland, Philip M., "An Album of Map Projections",
    U.S. Geological Survey Professional Paper 1453 , United State Government
    Printing Office, Washington D.C., 1989.
*******************************************************************************/
Proj4js.Proj.equi = {

  init: function() {
    if(!this.x0) this.x0=0;
    if(!this.y0) this.y0=0;
    if(!this.lat0) this.lat0=0;
    if(!this.long0) this.long0=0;
    ///this.t2;
  },



/* Equirectangular forward equations--mapping lat,long to x,y
  ---------------------------------------------------------*/
  forward: function(p) {

    var lon=p.x;        
    var lat=p.y;      

    var dlon = Proj4js.common.adjust_lon(lon - this.long0);
    var x = this.x0 +this. a * dlon *Math.cos(this.lat0);
    var y = this.y0 + this.a * lat;

    this.t1=x;
    this.t2=Math.cos(this.lat0);
    p.x=x;
    p.y=y;
    return p;
  },  //equiFwd()



/* Equirectangular inverse equations--mapping x,y to lat/long
  ---------------------------------------------------------*/
  inverse: function(p) {

    p.x -= this.x0;
    p.y -= this.y0;
    var lat = p.y /this. a;

    if ( Math.abs(lat) > Proj4js.common.HALF_PI) {
        Proj4js.reportError("equi:Inv:DataError");
    }
    var lon = Proj4js.common.adjust_lon(this.long0 + p.x / (this.a * Math.cos(this.lat0)));
    p.x=lon;
    p.y=lat;
  }//equiInv()
};


/* ======================================================================
    projCode/merc.js
   ====================================================================== */

/*******************************************************************************
NAME                            MERCATOR

PURPOSE:  Transforms input longitude and latitude to Easting and
    Northing for the Mercator projection.  The
    longitude and latitude must be in radians.  The Easting
    and Northing values will be returned in meters.

PROGRAMMER              DATE
----------              ----
D. Steinwand, EROS      Nov, 1991
T. Mittan   Mar, 1993

ALGORITHM REFERENCES

1.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

2.  Snyder, John P. and Voxland, Philip M., "An Album of Map Projections",
    U.S. Geological Survey Professional Paper 1453 , United State Government
    Printing Office, Washington D.C., 1989.
*******************************************************************************/

//static double r_major = a;       /* major axis        */
//static double r_minor = b;       /* minor axis        */
//static double lon_center = long0;    /* Center longitude (projection center) */
//static double lat_origin =  lat0;    /* center latitude     */
//static double e,es;              /* eccentricity constants    */
//static double m1;                  /* small value m     */
//static double false_northing = y0;   /* y offset in meters      */
//static double false_easting = x0;    /* x offset in meters      */
//scale_fact = k0 

Proj4js.Proj.merc = {
  init : function() {
  //?this.temp = this.r_minor / this.r_major;
  //this.temp = this.b / this.a;
  //this.es = 1.0 - Math.sqrt(this.temp);
  //this.e = Math.sqrt( this.es );
  //?this.m1 = Math.cos(this.lat_origin) / (Math.sqrt( 1.0 - this.es * Math.sin(this.lat_origin) * Math.sin(this.lat_origin)));
  //this.m1 = Math.cos(0.0) / (Math.sqrt( 1.0 - this.es * Math.sin(0.0) * Math.sin(0.0)));
    if (this.lat_ts) {
      if (this.sphere) {
        this.k0 = Math.cos(this.lat_ts);
      } else {
        this.k0 = Proj4js.common.msfnz(this.es, Math.sin(this.lat_ts), Math.cos(this.lat_ts));
      }
    }
  },

/* Mercator forward equations--mapping lat,long to x,y
  --------------------------------------------------*/

  forward : function(p) { 
    //alert("ll2m coords : "+coords);
    var lon = p.x;
    var lat = p.y;
    // convert to radians
    if ( lat*Proj4js.common.R2D > 90.0 && 
          lat*Proj4js.common.R2D < -90.0 && 
          lon*Proj4js.common.R2D > 180.0 && 
          lon*Proj4js.common.R2D < -180.0) {
      Proj4js.reportError("merc:forward: llInputOutOfRange: "+ lon +" : " + lat);
      return null;
    }

    var x,y;
    if(Math.abs( Math.abs(lat) - Proj4js.common.HALF_PI)  <= Proj4js.common.EPSLN) {
      Proj4js.reportError("merc:forward: ll2mAtPoles");
      return null;
    } else {
      if (this.sphere) {
        x = this.x0 + this.a * this.k0 * Proj4js.common.adjust_lon(lon - this.long0);
        y = this.y0 + this.a * this.k0 * Math.log(Math.tan(Proj4js.common.FORTPI + 0.5*lat));
      } else {
        var sinphi = Math.sin(lat);
        var ts = Proj4js.common.tsfnz(this.e,lat,sinphi);
        x = this.x0 + this.a * this.k0 * Proj4js.common.adjust_lon(lon - this.long0);
        y = this.y0 - this.a * this.k0 * Math.log(ts);
      }
      p.x = x; 
      p.y = y;
      return p;
    }
  },


  /* Mercator inverse equations--mapping x,y to lat/long
  --------------------------------------------------*/
  inverse : function(p) { 

    var x = p.x - this.x0;
    var y = p.y - this.y0;
    var lon,lat;

    if (this.sphere) {
      lat = Proj4js.common.HALF_PI - 2.0 * Math.atan(Math.exp(-y / this.a * this.k0));
    } else {
      var ts = Math.exp(-y / (this.a * this.k0));
      lat = Proj4js.common.phi2z(this.e,ts);
      if(lat == -9999) {
        Proj4js.reportError("merc:inverse: lat = -9999");
        return null;
      }
    }
    lon = Proj4js.common.adjust_lon(this.long0+ x / (this.a * this.k0));

    p.x = lon;
    p.y = lat;
    return p;
  }
};


/* ======================================================================
    projCode/utm.js
   ====================================================================== */

/*******************************************************************************
NAME                            TRANSVERSE MERCATOR

PURPOSE:  Transforms input longitude and latitude to Easting and
    Northing for the Transverse Mercator projection.  The
    longitude and latitude must be in radians.  The Easting
    and Northing values will be returned in meters.

ALGORITHM REFERENCES

1.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

2.  Snyder, John P. and Voxland, Philip M., "An Album of Map Projections",
    U.S. Geological Survey Professional Paper 1453 , United State Government
    Printing Office, Washington D.C., 1989.
*******************************************************************************/


/**
  Initialize Transverse Mercator projection
*/

Proj4js.Proj.utm = {
  dependsOn : 'tmerc',

  init : function() {
    if (!this.zone) {
      Proj4js.reportError("utm:init: zone must be specified for UTM");
      return;
    }
    this.lat0 = 0.0;
    this.long0 = ((6 * Math.abs(this.zone)) - 183) * Proj4js.common.D2R;
    this.x0 = 500000.0;
    this.y0 = this.utmSouth ? 10000000.0 : 0.0;
    this.k0 = 0.9996;

    Proj4js.Proj['tmerc'].init.apply(this);
    this.forward = Proj4js.Proj['tmerc'].forward;
    this.inverse = Proj4js.Proj['tmerc'].inverse;
  }
};
/* ======================================================================
    projCode/eqdc.js
   ====================================================================== */

/*******************************************************************************
NAME                            EQUIDISTANT CONIC 

PURPOSE:  Transforms input longitude and latitude to Easting and Northing
    for the Equidistant Conic projection.  The longitude and
    latitude must be in radians.  The Easting and Northing values
    will be returned in meters.

PROGRAMMER              DATE
----------              ----
T. Mittan   Mar, 1993

ALGORITHM REFERENCES

1.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

2.  Snyder, John P. and Voxland, Philip M., "An Album of Map Projections",
    U.S. Geological Survey Professional Paper 1453 , United State Government
    Printing Office, Washington D.C., 1989.
*******************************************************************************/

/* Variables common to all subroutines in this code file
  -----------------------------------------------------*/

Proj4js.Proj.eqdc = {

/* Initialize the Equidistant Conic projection
  ------------------------------------------*/
  init: function() {

    /* Place parameters in static storage for common use
      -------------------------------------------------*/

    if(!this.mode) this.mode=0;//chosen default mode
    this.temp = this.b / this.a;
    this.es = 1.0 - Math.pow(this.temp,2);
    this.e = Math.sqrt(this.es);
    this.e0 = Proj4js.common.e0fn(this.es);
    this.e1 = Proj4js.common.e1fn(this.es);
    this.e2 = Proj4js.common.e2fn(this.es);
    this.e3 = Proj4js.common.e3fn(this.es);

    this.sinphi=Math.sin(this.lat1);
    this.cosphi=Math.cos(this.lat1);

    this.ms1 = Proj4js.common.msfnz(this.e,this.sinphi,this.cosphi);
    this.ml1 = Proj4js.common.mlfn(this.e0, this.e1, this.e2,this.e3, this.lat1);

    /* format B
    ---------*/
    if (this.mode != 0) {
      if (Math.abs(this.lat1 + this.lat2) < Proj4js.common.EPSLN) {
            Proj4js.reportError("eqdc:Init:EqualLatitudes");
            //return(81);
       }
       this.sinphi=Math.sin(this.lat2);
       this.cosphi=Math.cos(this.lat2);   

       this.ms2 = Proj4js.common.msfnz(this.e,this.sinphi,this.cosphi);
       this.ml2 = Proj4js.common.mlfn(this.e0, this.e1, this.e2, this.e3, this.lat2);
       if (Math.abs(this.lat1 - this.lat2) >= Proj4js.common.EPSLN) {
         this.ns = (this.ms1 - this.ms2) / (this.ml2 - this.ml1);
       } else {
          this.ns = this.sinphi;
       }
    } else {
      this.ns = this.sinphi;
    }
    this.g = this.ml1 + this.ms1/this.ns;
    this.ml0 = Proj4js.common.mlfn(this.e0, this.e1,this. e2, this.e3, this.lat0);
    this.rh = this.a * (this.g - this.ml0);
  },


/* Equidistant Conic forward equations--mapping lat,long to x,y
  -----------------------------------------------------------*/
  forward: function(p) {
    var lon=p.x;
    var lat=p.y;

    /* Forward equations
      -----------------*/
    var ml = Proj4js.common.mlfn(this.e0, this.e1, this.e2, this.e3, lat);
    var rh1 = this.a * (this.g - ml);
    var theta = this.ns * Proj4js.common.adjust_lon(lon - this.long0);

    var x = this.x0  + rh1 * Math.sin(theta);
    var y = this.y0 + this.rh - rh1 * Math.cos(theta);
    p.x=x;
    p.y=y;
    return p;
  },

/* Inverse equations
  -----------------*/
  inverse: function(p) {
    p.x -= this.x0;
    p.y  = this.rh - p.y + this.y0;
    var con, rh1;
    if (this.ns >= 0) {
       var rh1 = Math.sqrt(p.x *p.x + p.y * p.y); 
       var con = 1.0;
    } else {
       rh1 = -Math.sqrt(p.x *p. x +p. y * p.y); 
       con = -1.0;
    }
    var theta = 0.0;
    if (rh1 != 0.0) theta = Math.atan2(con *p.x, con *p.y);
    var ml = this.g - rh1 /this.a;
    var lat = this.phi3z(this.ml,this.e0,this.e1,this.e2,this.e3);
    var lon = Proj4js.common.adjust_lon(this.long0 + theta / this.ns);

     p.x=lon;
     p.y=lat;  
     return p;
    },
    
/* Function to compute latitude, phi3, for the inverse of the Equidistant
   Conic projection.
-----------------------------------------------------------------*/
  phi3z: function(ml,e0,e1,e2,e3) {
    var phi;
    var dphi;

    phi = ml;
    for (var i = 0; i < 15; i++) {
      dphi = (ml + e1 * Math.sin(2.0 * phi) - e2 * Math.sin(4.0 * phi) + e3 * Math.sin(6.0 * phi))/ e0 - phi;
      phi += dphi;
      if (Math.abs(dphi) <= .0000000001) {
        return phi;
      }
    }
    Proj4js.reportError("PHI3Z-CONV:Latitude failed to converge after 15 iterations");
    return null;
  }

    
};
/* ======================================================================
    projCode/tmerc.js
   ====================================================================== */

/*******************************************************************************
NAME                            TRANSVERSE MERCATOR

PURPOSE:  Transforms input longitude and latitude to Easting and
    Northing for the Transverse Mercator projection.  The
    longitude and latitude must be in radians.  The Easting
    and Northing values will be returned in meters.

ALGORITHM REFERENCES

1.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

2.  Snyder, John P. and Voxland, Philip M., "An Album of Map Projections",
    U.S. Geological Survey Professional Paper 1453 , United State Government
    Printing Office, Washington D.C., 1989.
*******************************************************************************/


/**
  Initialize Transverse Mercator projection
*/

Proj4js.Proj.tmerc = {
  init : function() {
    this.e0 = Proj4js.common.e0fn(this.es);
    this.e1 = Proj4js.common.e1fn(this.es);
    this.e2 = Proj4js.common.e2fn(this.es);
    this.e3 = Proj4js.common.e3fn(this.es);
    this.ml0 = this.a * Proj4js.common.mlfn(this.e0, this.e1, this.e2, this.e3, this.lat0);
  },

  /**
    Transverse Mercator Forward  - long/lat to x/y
    long/lat in radians
  */
  forward : function(p) {
    var lon = p.x;
    var lat = p.y;

    var delta_lon = Proj4js.common.adjust_lon(lon - this.long0); // Delta longitude
    var con;    // cone constant
    var x, y;
    var sin_phi=Math.sin(lat);
    var cos_phi=Math.cos(lat);

    if (this.sphere) {  /* spherical form */
      var b = cos_phi * Math.sin(delta_lon);
      if ((Math.abs(Math.abs(b) - 1.0)) < .0000000001)  {
        Proj4js.reportError("tmerc:forward: Point projects into infinity");
        return(93);
      } else {
        x = .5 * this.a * this.k0 * Math.log((1.0 + b)/(1.0 - b));
        con = Math.acos(cos_phi * Math.cos(delta_lon)/Math.sqrt(1.0 - b*b));
        if (lat < 0) con = - con;
        y = this.a * this.k0 * (con - this.lat0);
      }
    } else {
      var al  = cos_phi * delta_lon;
      var als = Math.pow(al,2);
      var c   = this.ep2 * Math.pow(cos_phi,2);
      var tq  = Math.tan(lat);
      var t   = Math.pow(tq,2);
      con = 1.0 - this.es * Math.pow(sin_phi,2);
      var n   = this.a / Math.sqrt(con);
      var ml  = this.a * Proj4js.common.mlfn(this.e0, this.e1, this.e2, this.e3, lat);

      x = this.k0 * n * al * (1.0 + als / 6.0 * (1.0 - t + c + als / 20.0 * (5.0 - 18.0 * t + Math.pow(t,2) + 72.0 * c - 58.0 * this.ep2))) + this.x0;
      y = this.k0 * (ml - this.ml0 + n * tq * (als * (0.5 + als / 24.0 * (5.0 - t + 9.0 * c + 4.0 * Math.pow(c,2) + als / 30.0 * (61.0 - 58.0 * t + Math.pow(t,2) + 600.0 * c - 330.0 * this.ep2))))) + this.y0;

    }
    p.x = x; p.y = y;
    return p;
  }, // tmercFwd()

  /**
    Transverse Mercator Inverse  -  x/y to long/lat
  */
  inverse : function(p) {
    var con, phi;  /* temporary angles       */
    var delta_phi; /* difference between longitudes    */
    var i;
    var max_iter = 6;      /* maximun number of iterations */
    var lat, lon;

    if (this.sphere) {   /* spherical form */
      var f = Math.exp(p.x/(this.a * this.k0));
      var g = .5 * (f - 1/f);
      var temp = this.lat0 + p.y/(this.a * this.k0);
      var h = Math.cos(temp);
      con = Math.sqrt((1.0 - h * h)/(1.0 + g * g));
      lat = Math.asinz(con);
      if (temp < 0)
        lat = -lat;
      if ((g == 0) && (h == 0)) {
        lon = this.long0;
      } else {
        lon = Proj4js.common.adjust_lon(Math.atan2(g,h) + this.long0);
      }
    } else {    // ellipsoidal form
      var x = p.x - this.x0;
      var y = p.y - this.y0;

      con = (this.ml0 + y / this.k0) / this.a;
      phi = con;
      for (i=0;;i++) {
        delta_phi=((con + this.e1 * Math.sin(2.0*phi) - this.e2 * Math.sin(4.0*phi) + this.e3 * Math.sin(6.0*phi)) / this.e0) - phi;
        phi += delta_phi;
        if (Math.abs(delta_phi) <= Proj4js.common.EPSLN) break;
        if (i >= max_iter) {
          Proj4js.reportError("tmerc:inverse: Latitude failed to converge");
          return(95);
        }
      } // for()
      if (Math.abs(phi) < Proj4js.common.HALF_PI) {
        // sincos(phi, &sin_phi, &cos_phi);
        var sin_phi=Math.sin(phi);
        var cos_phi=Math.cos(phi);
        var tan_phi = Math.tan(phi);
        var c = this.ep2 * Math.pow(cos_phi,2);
        var cs = Math.pow(c,2);
        var t = Math.pow(tan_phi,2);
        var ts = Math.pow(t,2);
        con = 1.0 - this.es * Math.pow(sin_phi,2);
        var n = this.a / Math.sqrt(con);
        var r = n * (1.0 - this.es) / con;
        var d = x / (n * this.k0);
        var ds = Math.pow(d,2);
        lat = phi - (n * tan_phi * ds / r) * (0.5 - ds / 24.0 * (5.0 + 3.0 * t + 10.0 * c - 4.0 * cs - 9.0 * this.ep2 - ds / 30.0 * (61.0 + 90.0 * t + 298.0 * c + 45.0 * ts - 252.0 * this.ep2 - 3.0 * cs)));
        lon = Proj4js.common.adjust_lon(this.long0 + (d * (1.0 - ds / 6.0 * (1.0 + 2.0 * t + c - ds / 20.0 * (5.0 - 2.0 * c + 28.0 * t - 3.0 * cs + 8.0 * this.ep2 + 24.0 * ts))) / cos_phi));
      } else {
        lat = Proj4js.common.HALF_PI * Proj4js.common.sign(y);
        lon = this.long0;
      }
    }
    p.x = lon;
    p.y = lat;
    return p;
  } // tmercInv()
};
/* ======================================================================
    defs/GOOGLE.js
   ====================================================================== */

Proj4js.defs["GOOGLE"]="+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs";
Proj4js.defs["EPSG:900913"]=Proj4js.defs["GOOGLE"];
/* ======================================================================
    projCode/ortho.js
   ====================================================================== */

/*******************************************************************************
NAME                             ORTHOGRAPHIC 

PURPOSE:  Transforms input longitude and latitude to Easting and
    Northing for the Orthographic projection.  The
    longitude and latitude must be in radians.  The Easting
    and Northing values will be returned in meters.

PROGRAMMER              DATE
----------              ----
T. Mittan   Mar, 1993

ALGORITHM REFERENCES

1.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

2.  Snyder, John P. and Voxland, Philip M., "An Album of Map Projections",
    U.S. Geological Survey Professional Paper 1453 , United State Government
    Printing Office, Washington D.C., 1989.
*******************************************************************************/

Proj4js.Proj.ortho = {

  /* Initialize the Orthographic projection
    -------------------------------------*/
  init: function(def) {
    //double temp;      /* temporary variable   */

    /* Place parameters in static storage for common use
      -------------------------------------------------*/;
    this.sin_p14=Math.sin(this.lat0);
    this.cos_p14=Math.cos(this.lat0); 
  },


  /* Orthographic forward equations--mapping lat,long to x,y
    ---------------------------------------------------*/
  forward: function(p) {
    var sinphi, cosphi; /* sin and cos value        */
    var dlon;   /* delta longitude value      */
    var coslon;   /* cos of longitude       */
    var ksp;    /* scale factor         */
    var g;    
    var lon=p.x;
    var lat=p.y;  
    /* Forward equations
      -----------------*/
    dlon = Proj4js.common.adjust_lon(lon - this.long0);

    sinphi=Math.sin(lat);
    cosphi=Math.cos(lat); 

    coslon = Math.cos(dlon);
    g = this.sin_p14 * sinphi + this.cos_p14 * cosphi * coslon;
    ksp = 1.0;
    if ((g > 0) || (Math.abs(g) <= Proj4js.common.EPSLN)) {
      var x = this.a * ksp * cosphi * Math.sin(dlon);
      var y = this.y0 + this.a * ksp * (this.cos_p14 * sinphi - this.sin_p14 * cosphi * coslon);
    } else {
      Proj4js.reportError("orthoFwdPointError");
    }
    p.x=x;
    p.y=y;
    return p;
  },


  inverse: function(p) {
    var rh;   /* height above ellipsoid     */
    var z;    /* angle          */
    var sinz,cosz;  /* sin of z and cos of z      */
    var temp;
    var con;
    var lon , lat;
    /* Inverse equations
      -----------------*/
    p.x -= this.x0;
    p.y -= this.y0;
    rh = Math.sqrt(p.x * p.x + p.y * p.y);
    if (rh > this.a + .0000001) {
      Proj4js.reportError("orthoInvDataError");
    }
    z = Proj4js.common.asinz(rh / this.a);

    sinz=Math.sin(z);
    cosi=Math.cos(z);

    lon = this.long0;
    if (Math.abs(rh) <= Proj4js.common.EPSLN) {
      lat = this.lat0; 
    }
    lat = Proj4js.common.asinz(cosz * this.sin_p14 + (y * sinz * this.cos_p14)/rh);
    con = Math.abs(lat0) - Proj4js.common.HALF_PI;
    if (Math.abs(con) <= Proj4js.common.EPSLN) {
       if (this.lat0 >= 0) {
          lon = Proj4js.common.adjust_lon(this.long0 + Math.atan2(p.x, -p.y));
       } else {
          lon = Proj4js.common.adjust_lon(this.long0 -Math.atan2(-p.x, p.y));
       }
    }
    con = cosz - this.sin_p14 * Math.sin(lat);
    if ((Math.abs(con) >= Proj4js.common.EPSLN) || (Math.abs(x) >= Proj4js.common.EPSLN)) {
       lon = Proj4js.common.adjust_lon(this.long0 + Math.atan2((p.x * sinz * this.cos_p14), (con * rh)));
    }
    p.x=lon;
    p.y=lat;
    return p;
  }
};


/* ======================================================================
    projCode/stere.js
   ====================================================================== */


// Initialize the Stereographic projection

Proj4js.Proj.stere = {
  ssfn_: function(phit, sinphi, eccen) {
    sinphi *= eccen;
    return (Math.tan (.5 * (Proj4js.common.HALF_PI + phit)) * Math.pow((1. - sinphi) / (1. + sinphi), .5 * eccen));
  },
  TOL:  1.e-8,
  NITER:  8,
  CONV: 1.e-10,
  S_POLE: 0,
  N_POLE: 1,
  OBLIQ:  2,
  EQUIT:  3,

  init : function() {
    this.phits = this.lat_ts ? this.lat_ts : Proj4js.common.HALF_PI;
    var t = Math.abs(this.lat0);
    if ((Math.abs(t) - Proj4js.common.HALF_PI) < Proj4js.common.EPSLN) {
      this.mode = this.lat0 < 0. ? this.S_POLE : this.N_POLE;
    } else {
      this.mode = t > Proj4js.common.EPSLN ? this.OBLIQ : this.EQUIT;
    }
    this.phits = Math.abs(this.phits);
    if (this.es) {
      var X;

      switch (this.mode) {
      case this.N_POLE:
      case this.S_POLE:
        if (Math.abs(this.phits - Proj4js.common.HALF_PI) < Proj4js.common.EPSLN) {
          this.akm1 = 2. * this.k0 / Math.sqrt(Math.pow(1+this.e,1+this.e)*Math.pow(1-this.e,1-this.e));
        } else {
          t = Math.sin(this.phits);
          this.akm1 = Math.cos(this.phits) / Proj4js.common.tsfnz(this.e, this.phits, t);
          t *= this.e;
          this.akm1 /= Math.sqrt(1. - t * t);
        }
        break;
      case this.EQUIT:
        this.akm1 = 2. * this.k0;
        break;
      case this.OBLIQ:
        t = Math.sin(this.lat0);
        X = 2. * Math.atan(this.ssfn_(this.lat0, t, this.e)) - Proj4js.common.HALF_PI;
        t *= this.e;
        this.akm1 = 2. * this.k0 * Math.cos(this.lat0) / Math.sqrt(1. - t * t);
        this.sinX1 = Math.sin(X);
        this.cosX1 = Math.cos(X);
        break;
      }
    } else {
      switch (this.mode) {
      case this.OBLIQ:
        this.sinph0 = Math.sin(this.lat0);
        this.cosph0 = Math.cos(this.lat0);
      case this.EQUIT:
        this.akm1 = 2. * this.k0;
        break;
      case this.S_POLE:
      case this.N_POLE:
        this.akm1 = Math.abs(this.phits - Proj4js.common.HALF_PI) >= Proj4js.common.EPSLN ?
           Math.cos(this.phits) / Math.tan(Proj4js.common.FORTPI - .5 * this.phits) :
           2. * this.k0 ;
        break;
      }
    }
  }, 

// Stereographic forward equations--mapping lat,long to x,y
  forward: function(p) {
    var lon = p.x;
    var lat = p.y;
    var x, y
    
    if (this.sphere) {
      var  sinphi, cosphi, coslam, sinlam;

      sinphi = Math.sin(lat);
      cosphi = Math.cos(lat);
      coslam = Math.cos(lon);
      sinlam = Math.sin(lon);
      switch (this.mode) {
      case this.EQUIT:
        y = 1. + cosphi * coslam;
        if (y <= Proj4js.common.EPSLN) {
          F_ERROR;
        }
        y = this.akm1 / y;
        x = y * cosphi * sinlam;
        y *= sinphi;
        break;
      case this.OBLIQ:
        y = 1. + this.sinph0 * sinphi + this.cosph0 * cosphi * coslam;
        if (y <= Proj4js.common.EPSLN) {
          F_ERROR;
        }
        y = this.akm1 / y;
        x = y * cosphi * sinlam;
        y *= this.cosph0 * sinphi - this.sinph0 * cosphi * coslam;
        break;
      case this.N_POLE:
        coslam = -coslam;
        lat = -lat;
        //Note: no break here so it conitnues through S_POLE
      case this.S_POLE:
        if (Math.abs(lat - Proj4js.common.HALF_PI) < this.TOL) {
          F_ERROR;
        }
        y = this.akm1 * Math.tan(Proj4js.common.FORTPI + .5 * lat)
        x = sinlam * y;
        y *= coslam;
        break;
      }
    } else {
      coslam = Math.cos(lon);
      sinlam = Math.sin(lon);
      sinphi = Math.sin(lat);
      if (this.mode == this.OBLIQ || this.mode == this.EQUIT) {
        X = 2. * Math.atan(this.ssfn_(lat, sinphi, this.e));
        sinX = Math.sin(X - Proj4js.common.HALF_PI);
        cosX = Math.cos(X);
      }
      switch (this.mode) {
      case this.OBLIQ:
        A = this.akm1 / (this.cosX1 * (1. + this.sinX1 * sinX + this.cosX1 * cosX * coslam));
        y = A * (this.cosX1 * sinX - this.sinX1 * cosX * coslam);
        x = A * cosX;
        break;
      case this.EQUIT:
        A = 2. * this.akm1 / (1. + cosX * coslam);
        y = A * sinX;
        x = A * cosX;
        break;
      case this.S_POLE:
        lat = -lat;
        coslam = - coslam;
        sinphi = -sinphi;
      case this.N_POLE:
        x = this.akm1 * Proj4js.common.tsfnz(this.e, lat, sinphi);
        y = - x * coslam;
        break;
      }
      x = x * sinlam;
    }
    p.x = x*this.a + this.x0;
    p.y = y*this.a + this.y0;
    return p;
  },


//* Stereographic inverse equations--mapping x,y to lat/long
  inverse: function(p) {
    var x = (p.x - this.x0)/this.a;   /* descale and de-offset */
    var y = (p.y - this.y0)/this.a;
    var lon, lat

    var cosphi, sinphi, tp=0.0, phi_l=0.0, rho, halfe=0.0, pi2=0.0;
    var i;

    if (this.sphere) {
      var  c, rh, sinc, cosc;

      rh = Math.sqrt(x*x + y*y);
      c = 2. * Math.atan(rh / this.akm1);
      sinc = Math.sin(c);
      cosc = Math.cos(c);
      lon = 0.;
      switch (this.mode) {
      case this.EQUIT:
        if (Math.abs(rh) <= Proj4js.common.EPSLN) {
          lat = 0.;
        } else {
          lat = Math.asin(y * sinc / rh);
        }
        if (cosc != 0. || x != 0.) lon = Math.atan2(x * sinc, cosc * rh);
        break;
      case this.OBLIQ:
        if (Math.abs(rh) <= Proj4js.common.EPSLN) {
          lat = this.phi0;
        } else {
          lat = Math.asin(cosc * sinph0 + y * sinc * cosph0 / rh);
        }
        c = cosc - sinph0 * Math.sin(lat);
        if (c != 0. || x != 0.) {
          lon = Math.atan2(x * sinc * cosph0, c * rh);
        }
        break;
      case this.N_POLE:
        y = -y;
      case this.S_POLE:
        if (Math.abs(rh) <= Proj4js.common.EPSLN) {
          lat = this.phi0;
        } else {
          lat = Math.asin(this.mode == this.S_POLE ? -cosc : cosc);
        }
        lon = (x == 0. && y == 0.) ? 0. : Math.atan2(x, y);
        break;
      }
    } else {
      rho = Math.sqrt(x*x + y*y);
      switch (this.mode) {
      case this.OBLIQ:
      case this.EQUIT:
        tp = 2. * Math.atan2(rho * this.cosX1 , this.akm1);
        cosphi = Math.cos(tp);
        sinphi = Math.sin(tp);
        if( rho == 0.0 ) {
          phi_l = Math.asin(cosphi * this.sinX1);
        } else {
          phi_l = Math.asin(cosphi * this.sinX1 + (y * sinphi * this.cosX1 / rho));
        }

        tp = Math.tan(.5 * (Proj4js.common.HALF_PI + phi_l));
        x *= sinphi;
        y = rho * this.cosX1 * cosphi - y * this.sinX1* sinphi;
        pi2 = Proj4js.common.HALF_PI;
        halfe = .5 * this.e;
        break;
      case this.N_POLE:
        y = -y;
      case this.S_POLE:
        tp = - rho / this.akm1
        phi_l = Proj4js.common.HALF_PI - 2. * Math.atan(tp);
        pi2 = -Proj4js.common.HALF_PI;
        halfe = -.5 * this.e;
        break;
      }
      for (i = this.NITER; i--; phi_l = lat) { //check this
        sinphi = this.e * Math.sin(phi_l);
        lat = 2. * Math.atan(tp * Math.pow((1.+sinphi)/(1.-sinphi), halfe)) - pi2;
        if (Math.abs(phi_l - lat) < this.CONV) {
          if (this.mode == this.S_POLE) lat = -lat;
          lon = (x == 0. && y == 0.) ? 0. : Math.atan2(x, y);
          p.x = lon;
          p.y = lat
          return p;
        }
      }
    }
  }
}; 
/* ======================================================================
    projCode/mill.js
   ====================================================================== */

/*******************************************************************************
NAME                    MILLER CYLINDRICAL 

PURPOSE:  Transforms input longitude and latitude to Easting and
    Northing for the Miller Cylindrical projection.  The
    longitude and latitude must be in radians.  The Easting
    and Northing values will be returned in meters.

PROGRAMMER              DATE            
----------              ----           
T. Mittan   March, 1993

This function was adapted from the Lambert Azimuthal Equal Area projection
code (FORTRAN) in the General Cartographic Transformation Package software
which is available from the U.S. Geological Survey National Mapping Division.
 
ALGORITHM REFERENCES

1.  "New Equal-Area Map Projections for Noncircular Regions", John P. Snyder,
    The American Cartographer, Vol 15, No. 4, October 1988, pp. 341-355.

2.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

3.  "Software Documentation for GCTP General Cartographic Transformation
    Package", U.S. Geological Survey National Mapping Division, May 1982.
*******************************************************************************/

Proj4js.Proj.mill = {

/* Initialize the Miller Cylindrical projection
  -------------------------------------------*/
  init: function() {
    //no-op
  },


  /* Miller Cylindrical forward equations--mapping lat,long to x,y
    ------------------------------------------------------------*/
  forward: function(p) {
    var lon=p.x;
    var lat=p.y;
    /* Forward equations
      -----------------*/
    dlon = Proj4js.common.adjust_lon(lon -this.long0);
    var x = this.x0 +this.R * dlon;
    var y = this.y0 + this.R *Math.log(Math.tan((Proj4js.common.PI / 4.0) + (lat / 2.5))) * 1.25;

    p.x=x;
    p.y=y;
    return p;
  },//millFwd()

  /* Miller Cylindrical inverse equations--mapping x,y to lat/long
    ------------------------------------------------------------*/
  inverse: function(p) {
    p. x -= this.x0;
    p. y -= this.y0;

    var lon = Proj4js.common.adjust_lon(this.long0 + p.x /this.R);
    var lat = 2.5 * (Math.atan(Math.exp(p.y/ this.R / 1.25)) - Proj4js.common.PI / 4.0);

    p.x=lon;
    p.y=lat;
    return p;
  }//millInv()
};
/* ======================================================================
    projCode/sinu.js
   ====================================================================== */

/*******************************************************************************
NAME                      SINUSOIDAL

PURPOSE:  Transforms input longitude and latitude to Easting and
    Northing for the Sinusoidal projection.  The
    longitude and latitude must be in radians.  The Easting
    and Northing values will be returned in meters.

PROGRAMMER              DATE            
----------              ----           
D. Steinwand, EROS      May, 1991     

This function was adapted from the Sinusoidal projection code (FORTRAN) in the 
General Cartographic Transformation Package software which is available from 
the U.S. Geological Survey National Mapping Division.
 
ALGORITHM REFERENCES

1.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

2.  "Software Documentation for GCTP General Cartographic Transformation
    Package", U.S. Geological Survey National Mapping Division, May 1982.
*******************************************************************************/

Proj4js.Proj.sinu = {

  /* Initialize the Sinusoidal projection
    ------------------------------------*/
  init: function() {
    /* Place parameters in static storage for common use
      -------------------------------------------------*/
    this.R = 6370997.0; //Radius of earth
  },

  /* Sinusoidal forward equations--mapping lat,long to x,y
  -----------------------------------------------------*/
  forward: function(p) {
    var x,y,delta_lon;  
    var lon=p.x;
    var lat=p.y;  
    /* Forward equations
    -----------------*/
    delta_lon = Proj4js.common.adjust_lon(lon - this.long0);
    x = this.R * delta_lon * Math.cos(lat) + this.x0;
    y = this.R * lat + this.y0;

    p.x=x;
    p.y=y;  
    return p;
  },

  inverse: function(p) {
    var lat,temp,lon; 

    /* Inverse equations
      -----------------*/
    p.x -= this.x0;
    p.y -= this.y0;
    lat = p.y / this.R;
    if (Math.abs(lat) > Proj4js.common.HALF_PI) {
        Proj4js.reportError("sinu:Inv:DataError");
    }
    temp = Math.abs(lat) - Proj4js.common.HALF_PI;
    if (Math.abs(temp) > Proj4js.common.EPSLN) {
      temp = this.long0+ p.x / (this.R *Math.cos(lat));
      lon = Proj4js.common.adjust_lon(temp);
    } else {
      lon = this.long0;
    }
      
    p.x=lon;
    p.y=lat;
    return p;
  }
};


/* ======================================================================
    projCode/geocent.js
   ====================================================================== */

/*
Author:       Richard Greenwood rich@greenwoodmap.com
License:      LGPL as per: http://www.gnu.org/copyleft/lesser.html
*/

/**
 * convert between geodetic coordinates (longitude, latitude, height)
 * and gecentric coordinates (X, Y, Z)
 * ported from Proj 4.9.9 geocent.c
*/


// following constants #define'd in geocent.h
// var GEOCENT_NO_ERROR  = 0x0000;
var GEOCENT_LAT_ERROR = 0x0001;
// var GEOCENT_LON_ERROR = 0x0002;
// var cs.a_ERROR        = 0x0004;
// var cs.b_ERROR        = 0x0008;
// var cs.a_LESS_B_ERROR = 0x0010;

// following constants from geocent.c
var COS_67P5  = 0.38268343236508977;  /* cosine of 67.5 degrees */
var AD_C      = 1.0026000;            /* Toms region 1 constant */

function cs_geodetic_to_geocentric (cs, p) {

/*
 * The function Convert_Geodetic_To_Geocentric converts geodetic coordinates
 * (latitude, longitude, and height) to geocentric coordinates (X, Y, Z),
 * according to the current ellipsoid parameters.
 *
 *    Latitude  : Geodetic latitude in radians                     (input)
 *    Longitude : Geodetic longitude in radians                    (input)
 *    Height    : Geodetic height, in meters                       (input)
 *    X         : Calculated Geocentric X coordinate, in meters    (output)
 *    Y         : Calculated Geocentric Y coordinate, in meters    (output)
 *    Z         : Calculated Geocentric Z coordinate, in meters    (output)
 *
 */

  var Longitude = p.x;
  var Latitude = p.y;
  var Height = p.z;
  var X;  // output
  var Y;
  var Z;

  var Error_Code=0;  //  GEOCENT_NO_ERROR;
  var Rn;            /*  Earth radius at location  */
  var Sin_Lat;       /*  Math.sin(Latitude)  */
  var Sin2_Lat;      /*  Square of Math.sin(Latitude)  */
  var Cos_Lat;       /*  Math.cos(Latitude)  */

  /*
  ** Don't blow up if Latitude is just a little out of the value
  ** range as it may just be a rounding issue.  Also removed longitude
  ** test, it should be wrapped by Math.cos() and Math.sin().  NFW for PROJ.4, Sep/2001.
  */
  if( Latitude < -HALF_PI && Latitude > -1.001 * HALF_PI )
      Latitude = -HALF_PI;
  else if( Latitude > HALF_PI && Latitude < 1.001 * HALF_PI )
      Latitude = HALF_PI;
  else if ((Latitude < -HALF_PI) || (Latitude > HALF_PI))
  { /* Latitude out of range */
    Error_Code |= GEOCENT_LAT_ERROR;
  }

  if (!Error_Code)
  { /* no errors */
    if (Longitude > PI)
      Longitude -= (2*PI);
    Sin_Lat = Math.sin(Latitude);
    Cos_Lat = Math.cos(Latitude);
    Sin2_Lat = Sin_Lat * Sin_Lat;
    Rn = cs.a / (Math.sqrt(1.0e0 - cs.es * Sin2_Lat));
    X = (Rn + Height) * Cos_Lat * Math.cos(Longitude);
    Y = (Rn + Height) * Cos_Lat * Math.sin(Longitude);
    Z = ((Rn * (1 - cs.es)) + Height) * Sin_Lat;

  }

  p.x = X;
  p.y = Y;
  p.z = Z;
  return Error_Code;
} // cs_geodetic_to_geocentric()


/** Convert_Geocentric_To_Geodetic
 * The method used here is derived from 'An Improved Algorithm for
 * Geocentric to Geodetic Coordinate Conversion', by Ralph Toms, Feb 1996
 */

function cs_geocentric_to_geodetic (cs, p) {

  var X =p.x;
  var Y = p.y;
  var Z = p.z;
  var Longitude;
  var Latitude;
  var Height;

  var W;        /* distance from Z axis */
  var W2;       /* square of distance from Z axis */
  var T0;       /* initial estimate of vertical component */
  var T1;       /* corrected estimate of vertical component */
  var S0;       /* initial estimate of horizontal component */
  var S1;       /* corrected estimate of horizontal component */
  var Sin_B0;   /* Math.sin(B0), B0 is estimate of Bowring aux variable */
  var Sin3_B0;  /* cube of Math.sin(B0) */
  var Cos_B0;   /* Math.cos(B0) */
  var Sin_p1;   /* Math.sin(phi1), phi1 is estimated latitude */
  var Cos_p1;   /* Math.cos(phi1) */
  var Rn;       /* Earth radius at location */
  var Sum;      /* numerator of Math.cos(phi1) */
  var At_Pole;  /* indicates location is in polar region */

  X = parseFloat(X);  // cast from string to float
  Y = parseFloat(Y);
  Z = parseFloat(Z);

  At_Pole = false;
  if (X != 0.0)
  {
      Longitude = Math.atan2(Y,X);
  }
  else
  {
      if (Y > 0)
      {
          Longitude = HALF_PI;
      }
      else if (Y < 0)
      {
          Longitude = -HALF_PI;
      }
      else
      {
          At_Pole = true;
          Longitude = 0.0;
          if (Z > 0.0)
          {  /* north pole */
              Latitude = HALF_PI;
          }
          else if (Z < 0.0)
          {  /* south pole */
              Latitude = -HALF_PI;
          }
          else
          {  /* center of earth */
              Latitude = HALF_PI;
              Height = -cs.b;
              return;
          }
      }
  }
  W2 = X*X + Y*Y;
  W = Math.sqrt(W2);
  T0 = Z * AD_C;
  S0 = Math.sqrt(T0 * T0 + W2);
  Sin_B0 = T0 / S0;
  Cos_B0 = W / S0;
  Sin3_B0 = Sin_B0 * Sin_B0 * Sin_B0;
  T1 = Z + cs.b * cs.ep2 * Sin3_B0;
  Sum = W - cs.a * cs.es * Cos_B0 * Cos_B0 * Cos_B0;
  S1 = Math.sqrt(T1*T1 + Sum * Sum);
  Sin_p1 = T1 / S1;
  Cos_p1 = Sum / S1;
  Rn = cs.a / Math.sqrt(1.0 - cs.es * Sin_p1 * Sin_p1);
  if (Cos_p1 >= COS_67P5)
  {
      Height = W / Cos_p1 - Rn;
  }
  else if (Cos_p1 <= -COS_67P5)
  {
      Height = W / -Cos_p1 - Rn;
  }
  else
  {
      Height = Z / Sin_p1 + Rn * (cs.es - 1.0);
  }
  if (At_Pole == false)
  {
      Latitude = Math.atan(Sin_p1 / Cos_p1);
  }

  p.x = Longitude;
  p.y =Latitude;
  p.z = Height;
  return 0;
} // cs_geocentric_to_geodetic()



/****************************************************************/
// pj_geocentic_to_wgs84(defn, p )
//    defn = coordinate system definition,
//  p = point to transform in geocentric coordinates (x,y,z)
function cs_geocentric_to_wgs84( defn, p ) {

  if( defn.datum_type == PJD_3PARAM )
  {
    // if( x[io] == HUGE_VAL )
    //    continue;
    p.x += defn.datum_params[0];
    p.y += defn.datum_params[1];
    p.z += defn.datum_params[2];

  }
  else  // if( defn.datum_type == PJD_7PARAM )
  {
    var Dx_BF =defn.datum_params[0];
    var Dy_BF =defn.datum_params[1];
    var Dz_BF =defn.datum_params[2];
    var Rx_BF =defn.datum_params[3];
    var Ry_BF =defn.datum_params[4];
    var Rz_BF =defn.datum_params[5];
    var M_BF  =defn.datum_params[6];
    // if( x[io] == HUGE_VAL )
    //    continue;
    var x_out = M_BF*(       p.x - Rz_BF*p.y + Ry_BF*p.z) + Dx_BF;
    var y_out = M_BF*( Rz_BF*p.x +       p.y - Rx_BF*p.z) + Dy_BF;
    var z_out = M_BF*(-Ry_BF*p.x + Rx_BF*p.y +       p.z) + Dz_BF;
    p.x = x_out;
    p.y = y_out;
    p.z = z_out;
  }
} // cs_geocentric_to_wgs84

/****************************************************************/
// pj_geocentic_from_wgs84()
//  coordinate system definition,
//  point to transform in geocentric coordinates (x,y,z)
function cs_geocentric_from_wgs84( defn, p ) {

  if( defn.datum_type == PJD_3PARAM )
  {
    //if( x[io] == HUGE_VAL )
    //    continue;
    p.x -= defn.datum_params[0];
    p.y -= defn.datum_params[1];
    p.z -= defn.datum_params[2];

  }
  else // if( defn.datum_type == PJD_7PARAM )
  {
    var Dx_BF =defn.datum_params[0];
    var Dy_BF =defn.datum_params[1];
    var Dz_BF =defn.datum_params[2];
    var Rx_BF =defn.datum_params[3];
    var Ry_BF =defn.datum_params[4];
    var Rz_BF =defn.datum_params[5];
    var M_BF  =defn.datum_params[6];
    var x_tmp = (p.x - Dx_BF) / M_BF;
    var y_tmp = (p.y - Dy_BF) / M_BF;
    var z_tmp = (p.z - Dz_BF) / M_BF;
    //if( x[io] == HUGE_VAL )
    //    continue;

    p.x =        x_tmp + Rz_BF*y_tmp - Ry_BF*z_tmp;
    p.y = -Rz_BF*x_tmp +       y_tmp + Rx_BF*z_tmp;
    p.z =  Ry_BF*x_tmp - Rx_BF*y_tmp +       z_tmp;
  }
} //cs_geocentric_from_wgs84()
/* ======================================================================
    projCode/vandg.js
   ====================================================================== */

/*******************************************************************************
NAME                    VAN DER GRINTEN 

PURPOSE:  Transforms input Easting and Northing to longitude and
    latitude for the Van der Grinten projection.  The
    Easting and Northing must be in meters.  The longitude
    and latitude values will be returned in radians.

PROGRAMMER              DATE            
----------              ----           
T. Mittan   March, 1993

This function was adapted from the Van Der Grinten projection code
(FORTRAN) in the General Cartographic Transformation Package software
which is available from the U.S. Geological Survey National Mapping Division.
 
ALGORITHM REFERENCES

1.  "New Equal-Area Map Projections for Noncircular Regions", John P. Snyder,
    The American Cartographer, Vol 15, No. 4, October 1988, pp. 341-355.

2.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

3.  "Software Documentation for GCTP General Cartographic Transformation
    Package", U.S. Geological Survey National Mapping Division, May 1982.
*******************************************************************************/

Proj4js.Proj.vandg = {

/* Initialize the Van Der Grinten projection
  ----------------------------------------*/
  init: function() {
    this.R = 6370997.0; //Radius of earth
  },

  forward: function(p) {

    var lon=p.x;
    var lat=p.y;  

    /* Forward equations
    -----------------*/
    var dlon = Proj4js.common.adjust_lon(lon - this.long0);
    var x,y;

    if (Math.abs(lat) <= Proj4js.common.EPSLN) {
      x = this.x0  + this.R * dlon;
      y = this.y0;
    }
    var theta = Proj4js.common.asinz(2.0 * Math.abs(lat / Proj4js.common.PI));
    if ((Math.abs(dlon) <= Proj4js.common.EPSLN) || (Math.abs(Math.abs(lat) - Proj4js.common.HALF_PI) <= Proj4js.common.EPSLN)) {
      x = this.x0;
      if (lat >= 0) {
        y = this.y0 + Proj4js.common.PI * this.R * Math.tan(.5 * theta);
      } else {
        y = this.y0 + Proj4js.common.PI * this.R * - Math.tan(.5 * theta);
      }
      //  return(OK);
    }
    var al = .5 * Math.abs((Proj4js.common.PI / dlon) - (dlon / Proj4js.common.PI));
    var asq = al * al;
    var sinth = Math.sin(theta);
    var costh = Math.cos(theta);

    var g = costh / (sinth + costh - 1.0);
    var gsq = g * g;
    var m = g * (2.0 / sinth - 1.0);
    var msq = m * m;
    var con = Proj4js.common.PI * this.R * (al * (g - msq) + Math.sqrt(asq * (g - msq) * (g - msq) - (msq + asq) * (gsq - msq))) / (msq + asq);
    if (dlon < 0) {
     con = -con;
    }
    x = this.x0 + con;
    con = Math.abs(con / (Proj4js.common.PI * this.R));
    if (lat >= 0) {
     y = this.y0 + Proj4js.common.PI * this.R * Math.sqrt(1.0 - con * con - 2.0 * al * con);
    } else {
     y = this.y0 - Proj4js.common.PI * this.R * Math.sqrt(1.0 - con * con - 2.0 * al * con);
    }
    p.x = x;
    p.y = y;
    return p;
  },

/* Van Der Grinten inverse equations--mapping x,y to lat/long
  ---------------------------------------------------------*/
  inverse: function(p) {
    var dlon;
    var xx,yy,xys,c1,c2,c3;
    var al,asq;
    var a1;
    var m1;
    var con;
    var th1;
    var d;

    /* inverse equations
    -----------------*/
    p.x -= this.x0;
    p.y -= this.y0;
    con = Proj4js.common.PI * this.R;
    xx = p.x / con;
    yy =p.y / con;
    xys = xx * xx + yy * yy;
    c1 = -Math.abs(yy) * (1.0 + xys);
    c2 = c1 - 2.0 * yy * yy + xx * xx;
    c3 = -2.0 * c1 + 1.0 + 2.0 * yy * yy + xys * xys;
    d = yy * yy / c3 + (2.0 * c2 * c2 * c2 / c3 / c3 / c3 - 9.0 * c1 * c2 / c3 /c3) / 27.0;
    a1 = (c1 - c2 * c2 / 3.0 / c3) / c3;
    m1 = 2.0 * Math.sqrt( -a1 / 3.0);
    con = ((3.0 * d) / a1) / m1;
    if (Math.abs(con) > 1.0) {
      if (con >= 0.0) {
        con = 1.0;
      } else {
        con = -1.0;
      }
    }
    th1 = Math.acos(con) / 3.0;
    if (p.y >= 0) {
      lat = (-m1 *Math.cos(th1 + Proj4js.common.PI / 3.0) - c2 / 3.0 / c3) * Proj4js.common.PI;
    } else {
      lat = -(-m1 * Math.cos(th1 + PI / 3.0) - c2 / 3.0 / c3) * Proj4js.common.PI;
    }

    if (Math.abs(xx) < Proj4js.common.EPSLN) {
      lon = this.long0;
    }
    lon = Proj4js.common.adjust_lon(this.long0 + Proj4js.common.PI * (xys - 1.0 + Math.sqrt(1.0 + 2.0 * (xx * xx - yy * yy) + xys * xys)) / 2.0 / xx);

    p.x=lon;
    p.y=lat;
    return p;
  }
};
/* ======================================================================
    projCode/gauss.js
   ====================================================================== */


Proj4js.Proj.gauss = {

  init : function() {
    sphi = Math.sin(this.lat0);
    cphi = Math.cos(this.lat0);  
    cphi *= cphi;
    this.rc = Math.sqrt(1.0 - this.es) / (1.0 - this.es * sphi * sphi);
    this.C = Math.sqrt(1.0 + this.es * cphi * cphi / (1.0 - this.es));
    this.phic0 = Math.asin(sphi / this.C);
    this.ratexp = 0.5 * this.C * this.e;
    this.K = Math.tan(0.5 * this.phic0 + Proj4js.common.FORTPI) / (Math.pow(Math.tan(0.5*this.lat0 + Proj4js.common.FORTPI), this.C) * Proj4js.common.srat(this.e*sphi, this.ratexp));
  },

  forward : function(p) {
    var lon = p.x;
    var lat = p.y;

    p.y = 2.0 * Math.atan( this.K * Math.pow(Math.tan(0.5 * lat + Proj4js.common.FORTPI), this.C) * Proj4js.common.srat(this.e * Math.sin(lat), this.ratexp) ) - Proj4js.common.HALF_PI;
    p.x = this.C * lon;
    return p;
  },

  inverse : function(p) {
    var DEL_TOL = 1e-14;
    var lon = p.x / this.C;
    var lat = p.y;
    num = Math.pow(Math.tan(0.5 * lat + Proj4js.common.FORTPI)/this.K, 1./this.C);
    for (var i = Proj4js.common.MAX_ITER; i>0; --i) {
      lat = 2.0 * Math.atan(num * Proj4js.common.srat(this.e * Math.sin(p.y), -0.5 * this.e)) - Proj4js.common.HALF_PI;
      if (Math.abs(lat - p.y) < DEL_TOL) break;
      p.y = lat;
    } 
    /* convergence failed */
    if (!i) {
      Proj4js.reportError("gauss:inverse:convergence failed");
      return null;
    }
    p.x = lon;
    p.y = lat;
    return p;
  }
};

/* ======================================================================
    projCode/omerc.js
   ====================================================================== */

/*******************************************************************************
NAME                       OBLIQUE MERCATOR (HOTINE) 

PURPOSE:  Transforms input longitude and latitude to Easting and
    Northing for the Oblique Mercator projection.  The
    longitude and latitude must be in radians.  The Easting
    and Northing values will be returned in meters.

PROGRAMMER              DATE
----------              ----
T. Mittan   Mar, 1993

ALGORITHM REFERENCES

1.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

2.  Snyder, John P. and Voxland, Philip M., "An Album of Map Projections",
    U.S. Geological Survey Professional Paper 1453 , United State Government
    Printing Office, Washington D.C., 1989.
*******************************************************************************/

Proj4js.Proj.omerc = {

  /* Initialize the Oblique Mercator  projection
    ------------------------------------------*/
  init: function() {
    if (!this.mode) this.mode=0;
    if (!this.lon1)   {this.lon1=0;this.mode=1;}
    if (!this.lon2)   this.lon2=0;
    if (!this.lat2)    this.lat2=0;

    /* Place parameters in static storage for common use
      -------------------------------------------------*/
    var temp = this.b/ this.a;
    var es = 1.0 - Math.pow(temp,2);
    var e = Math.sqrt(es);

    this.sin_p20=Math.sin(this.lat0);
    this.cos_p20=Math.cos(this.lat0);

    this.con = 1.0 - this.es * this.sin_p20 * this.sin_p20;
    this.com = Math.sqrt(1.0 - es);
    this.bl = Math.sqrt(1.0 + this.es * Math.pow(this.cos_p20,4.0)/(1.0 - es));
    this.al = this.a * this.bl * this.k0 * this.com / this.con;
    if (Math.abs(this.lat0) < Proj4js.common.EPSLN) {
       this.ts = 1.0;
       this.d = 1.0;
       this.el = 1.0;
    } else {
       this.ts = Proj4js.common.tsfnz(this.e,this.lat0,this.sin_p20);
       this.con = Math.sqrt(this.con);
       this.d = this.bl * this.com / (this.cos_p20 * this.con);
       if ((this.d * this.d - 1.0) > 0.0) {
          if (this.lat0 >= 0.0) {
             this.f = this.d + Math.sqrt(this.d * this.d - 1.0);
          } else {
             this.f = this.d - Math.sqrt(this.d * this.d - 1.0);
          }
       } else {
         this.f = this.d;
       }
       this.el = this.f * Math.pow(this.ts,this.bl);
    }

    //this.longc=52.60353916666667;

    if (this.mode != 0) {
       this.g = .5 * (this.f - 1.0/this.f);
       this.gama = Proj4js.common.asinz(Math.sin(this.alpha) / this.d);
       this.longc= this.longc - Proj4js.common.asinz(this.g * Math.tan(this.gama))/this.bl;

       /* Report parameters common to format B
       -------------------------------------*/
       //genrpt(azimuth * R2D,"Azimuth of Central Line:    ");
       //cenlon(lon_origin);
      // cenlat(lat_origin);

       this.con = Math.abs(this.lat0);
       if ((this.con > Proj4js.common.EPSLN) && (Math.abs(this.con - Proj4js.common.HALF_PI) > Proj4js.common.EPSLN)) {
            this.singam=Math.sin(this.gama);
            this.cosgam=Math.cos(this.gama);

            this.sinaz=Math.sin(this.alpha);
            this.cosaz=Math.cos(this.alpha);

            if (this.lat0>= 0) {
               this.u =  (this.al / this.bl) * Math.atan(Math.sqrt(this.d*this.d - 1.0)/this.cosaz);
            } else {
               this.u =  -(this.al / this.bl) *Math.atan(Math.sqrt(this.d*this.d - 1.0)/this.cosaz);
            }
          } else {
            Proj4js.reportError("omerc:Init:DataError");
          }
       } else {
       this.sinphi =Math. sin(this.at1);
       this.ts1 = Proj4js.common.tsfnz(this.e,this.lat1,this.sinphi);
       this.sinphi = Math.sin(this.lat2);
       this.ts2 = Proj4js.common.tsfnz(this.e,this.lat2,this.sinphi);
       this.h = Math.pow(this.ts1,this.bl);
       this.l = Math.pow(this.ts2,this.bl);
       this.f = this.el/this.h;
       this.g = .5 * (this.f - 1.0/this.f);
       this.j = (this.el * this.el - this.l * this.h)/(this.el * this.el + this.l * this.h);
       this.p = (this.l - this.h) / (this.l + this.h);
       this.dlon = this.lon1 - this.lon2;
       if (this.dlon < -Proj4js.common.PI) this.lon2 = this.lon2 - 2.0 * Proj4js.common.PI;
       if (this.dlon > Proj4js.common.PI) this.lon2 = this.lon2 + 2.0 * Proj4js.common.PI;
       this.dlon = this.lon1 - this.lon2;
       this.longc = .5 * (this.lon1 + this.lon2) -Math.atan(this.j * Math.tan(.5 * this.bl * this.dlon)/this.p)/this.bl;
       this.dlon  = Proj4js.common.adjust_lon(this.lon1 - this.longc);
       this.gama = Math.atan(Math.sin(this.bl * this.dlon)/this.g);
       this.alpha = Proj4js.common.asinz(this.d * Math.sin(this.gama));

       /* Report parameters common to format A
       -------------------------------------*/

       if (Math.abs(this.lat1 - this.lat2) <= Proj4js.common.EPSLN) {
          Proj4js.reportError("omercInitDataError");
          //return(202);
       } else {
          this.con = Math.abs(this.lat1);
       }
       if ((this.con <= Proj4js.common.EPSLN) || (Math.abs(this.con - HALF_PI) <= Proj4js.common.EPSLN)) {
           Proj4js.reportError("omercInitDataError");
                //return(202);
       } else {
         if (Math.abs(Math.abs(this.lat0) - Proj4js.common.HALF_PI) <= Proj4js.common.EPSLN) {
            Proj4js.reportError("omercInitDataError");
            //return(202);
         }
       }

       this.singam=Math.sin(this.gam);
       this.cosgam=Math.cos(this.gam);

       this.sinaz=Math.sin(this.alpha);
       this.cosaz=Math.cos(this.alpha);  


       if (this.lat0 >= 0) {
          this.u =  (this.al/this.bl) * Math.atan(Math.sqrt(this.d * this.d - 1.0)/this.cosaz);
       } else {
          this.u = -(this.al/this.bl) * Math.atan(Math.sqrt(this.d * this.d - 1.0)/this.cosaz);
       }
     }
  },


  /* Oblique Mercator forward equations--mapping lat,long to x,y
    ----------------------------------------------------------*/
  forward: function(p) {
    var theta;    /* angle          */
    var sin_phi, cos_phi;/* sin and cos value       */
    var b;    /* temporary values       */
    var c, t, tq; /* temporary values       */
    var con, n, ml; /* cone constant, small m     */
    var q,us,vl;
    var ul,vs;
    var s;
    var dlon;
    var ts1;

    var lon=p.x;
    var lat=p.y;
    /* Forward equations
      -----------------*/
    sin_phi = Math.sin(lat);
    dlon = Proj4js.common.adjust_lon(lon - this.longc);
    vl = Math.sin(this.bl * dlon);
    if (Math.abs(Math.abs(lat) - Proj4js.common.HALF_PI) > Proj4js.common.EPSLN) {
       ts1 = Proj4js.common.tsfnz(this.e,lat,sin_phi);
       q = this.el / (Math.pow(ts1,this.bl));
       s = .5 * (q - 1.0 / q);
       t = .5 * (q + 1.0/ q);
       ul = (s * this.singam - vl * this.cosgam) / t;
       con = Math.cos(this.bl * dlon);
       if (Math.abs(con) < .0000001) {
          us = this.al * this.bl * dlon;
       } else {
          us = this.al * Math.atan((s * this.cosgam + vl * this.singam) / con)/this.bl;
          if (con < 0) us = us + Proj4js.common.PI * this.al / this.bl;
       }
    } else {
       if (lat >= 0) {
          ul = this.singam;
       } else {
          ul = -this.singam;
       }
       us = this.al * lat / this.bl;
    }
    if (Math.abs(Math.abs(ul) - 1.0) <= Proj4js.common.EPSLN) {
       //alert("Point projects into infinity","omer-for");
       Proj4js.reportError("omercFwdInfinity");
       //return(205);
    }
    vs = .5 * this.al * Math.log((1.0 - ul)/(1.0 + ul)) / this.bl;
    us = us - this.u;
    var x = this.x0 + vs * this.cosaz + us * this.sinaz;
    var y = this.y0 + us * this.cosaz - vs * this.sinaz;

    p.x=x;
    p.y=y;
    return p;
  },

  inverse: function(p) {
    var delta_lon;  /* Delta longitude (Given longitude - center  */
    var theta;    /* angle          */
    var delta_theta;  /* adjusted longitude       */
    var sin_phi, cos_phi;/* sin and cos value       */
    var b;    /* temporary values       */
    var c, t, tq; /* temporary values       */
    var con, n, ml; /* cone constant, small m     */
    var vs,us,q,s,ts1;
    var vl,ul,bs;
    var dlon;
    var  flag;

    /* Inverse equations
      -----------------*/
    p.x -= this.x0;
    p.y -= this.y0;
    flag = 0;
    vs = p.x * this.cosaz - p.y * this.sinaz;
    us = p.y * this.cosaz + p.x * this.sinaz;
    us = us + this.u;
    q = Math.exp(-this.bl * vs / this.al);
    s = .5 * (q - 1.0/q);
    t = .5 * (q + 1.0/q);
    vl = Math.sin(this.bl * us / this.al);
    ul = (vl * this.cosgam + s * this.singam)/t;
    if (Math.abs(Math.abs(ul) - 1.0) <= Proj4js.common.EPSLN)
       {
       lon = this.longc;
       if (ul >= 0.0) {
          lat = Proj4js.common.HALF_PI;
       } else {
         lat = -Proj4js.common.HALF_PI;
       }
    } else {
       con = 1.0 / this.bl;
       ts1 =Math.pow((this.el / Math.sqrt((1.0 + ul) / (1.0 - ul))),con);
       lat = Proj4js.common.phi2z(this.e,ts1);
       //if (flag != 0)
          //return(flag);
       //~ con = Math.cos(this.bl * us /al);
       theta = this.longc - Math.atan2((s * this.cosgam - vl * this.singam) , con)/this.bl;
       lon = Proj4js.common.adjust_lon(theta);
    }
    p.x=lon;
    p.y=lat;
    return p;
  }
};
/* ======================================================================
    projCode/lcc.js
   ====================================================================== */

/*******************************************************************************
NAME                            LAMBERT CONFORMAL CONIC

PURPOSE:  Transforms input longitude and latitude to Easting and
    Northing for the Lambert Conformal Conic projection.  The
    longitude and latitude must be in radians.  The Easting
    and Northing values will be returned in meters.


ALGORITHM REFERENCES

1.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

2.  Snyder, John P. and Voxland, Philip M., "An Album of Map Projections",
    U.S. Geological Survey Professional Paper 1453 , United State Government
*******************************************************************************/


//<2104> +proj=lcc +lat_1=10.16666666666667 +lat_0=10.16666666666667 +lon_0=-71.60561777777777 +k_0=1 +x0=-17044 +x0=-23139.97 +ellps=intl +units=m +no_defs  no_defs

// Initialize the Lambert Conformal conic projection
// -----------------------------------------------------------------

//Proj4js.Proj.lcc = Class.create();
Proj4js.Proj.lcc = {
  init : function() {

    // array of:  r_maj,r_min,lat1,lat2,c_lon,c_lat,false_east,false_north
    //double c_lat;                   /* center latitude                      */
    //double c_lon;                   /* center longitude                     */
    //double lat1;                    /* first standard parallel              */
    //double lat2;                    /* second standard parallel             */
    //double r_maj;                   /* major axis                           */
    //double r_min;                   /* minor axis                           */
    //double false_east;              /* x offset in meters                   */
    //double false_north;             /* y offset in meters                   */

      if (!this.lat2){this.lat2=this.lat0;}//if lat2 is not defined
      if (!this.k0) this.k0 = 1.0;

    // Standard Parallels cannot be equal and on opposite sides of the equator
      if (Math.abs(this.lat1+this.lat2) < Proj4js.common.EPSLN) {
        Proj4js.reportError("lcc:init: Equal Latitudes");
        return;
      }

      var temp = this.b / this.a;
      this.e = Math.sqrt(1.0 - temp*temp);

      var sin1 = Math.sin(this.lat1);
      var cos1 = Math.cos(this.lat1);
      var ms1 = Proj4js.common.msfnz(this.e, sin1, cos1);
      var ts1 = Proj4js.common.tsfnz(this.e, this.lat1, sin1);

      var sin2 = Math.sin(this.lat2);
      var cos2 = Math.cos(this.lat2);
      var ms2 = Proj4js.common.msfnz(this.e, sin2, cos2);
      var ts2 = Proj4js.common.tsfnz(this.e, this.lat2, sin2);

      var ts0 = Proj4js.common.tsfnz(this.e, this.lat0, Math.sin(this.lat0));

      if (Math.abs(this.lat1 - this.lat2) > Proj4js.common.EPSLN) {
        this.ns = Math.log(ms1/ms2)/Math.log(ts1/ts2);
      } else {
        this.ns = sin1;
      }
      this.f0 = ms1 / (this.ns * Math.pow(ts1, this.ns));
      this.rh = this.a * this.f0 * Math.pow(ts0, this.ns);
      if (!this.title) this.title = "Lambert Conformal Conic";
    },


    // Lambert Conformal conic forward equations--mapping lat,long to x,y
    // -----------------------------------------------------------------
    forward : function(p) {

      var lon = p.x;
      var lat = p.y;

    // convert to radians
      if ( lat <= 90.0 && lat >= -90.0 && lon <= 180.0 && lon >= -180.0) {
        //lon = lon * Proj4js.common.D2R;
        //lat = lat * Proj4js.common.D2R;
      } else {
        Proj4js.reportError("lcc:forward: llInputOutOfRange: "+ lon +" : " + lat);
        return null;
      }

      var con  = Math.abs( Math.abs(lat) - Proj4js.common.HALF_PI);
      var ts;
      if (con > Proj4js.common.EPSLN) {
        ts = Proj4js.common.tsfnz(this.e, lat, Math.sin(lat) );
        rh1 = this.a * this.f0 * Math.pow(ts, this.ns);
      } else {
        con = lat * this.ns;
        if (con <= 0) {
          Proj4js.reportError("lcc:forward: No Projection");
          return null;
        }
        rh1 = 0;
      }
      var theta = this.ns * Proj4js.common.adjust_lon(lon - this.long0);
      p.x = this.k0 * (rh1 * Math.sin(theta)) + this.x0;
      p.y = this.k0 * (this.rh - rh1 * Math.cos(theta)) + this.y0;

      return p;
    },

  // Lambert Conformal Conic inverse equations--mapping x,y to lat/long
  // -----------------------------------------------------------------
  inverse : function(p) {

    var rh1, con, ts;
    var lat, lon;
    x = (p.x - this.x0)/this.k0;
    y = (this.rh - (p.y - this.y0)/this.k0);
    if (this.ns > 0) {
      rh1 = Math.sqrt (x * x + y * y);
      con = 1.0;
    } else {
      rh1 = -Math.sqrt (x * x + y * y);
      con = -1.0;
    }
    var theta = 0.0;
    if (rh1 != 0) {
      theta = Math.atan2((con * x),(con * y));
    }
    if ((rh1 != 0) || (this.ns > 0.0)) {
      con = 1.0/this.ns;
      ts = Math.pow((rh1/(this.a * this.f0)), con);
      lat = Proj4js.common.phi2z(this.e, ts);
      if (lat == -9999) return null;
    } else {
      lat = -Proj4js.common.HALF_PI;
    }
    lon = Proj4js.common.adjust_lon(theta/this.ns + this.long0);

    p.x = lon;
    p.y = lat;
    return p;
  }
};




/* ======================================================================
    projCode/laea.js
   ====================================================================== */

/*******************************************************************************
NAME                  LAMBERT AZIMUTHAL EQUAL-AREA
 
PURPOSE:  Transforms input longitude and latitude to Easting and
    Northing for the Lambert Azimuthal Equal-Area projection.  The
    longitude and latitude must be in radians.  The Easting
    and Northing values will be returned in meters.

PROGRAMMER              DATE            
----------              ----           
D. Steinwand, EROS      March, 1991   

This function was adapted from the Lambert Azimuthal Equal Area projection
code (FORTRAN) in the General Cartographic Transformation Package software
which is available from the U.S. Geological Survey National Mapping Division.
 
ALGORITHM REFERENCES

1.  "New Equal-Area Map Projections for Noncircular Regions", John P. Snyder,
    The American Cartographer, Vol 15, No. 4, October 1988, pp. 341-355.

2.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

3.  "Software Documentation for GCTP General Cartographic Transformation
    Package", U.S. Geological Survey National Mapping Division, May 1982.
*******************************************************************************/

Proj4js.Proj.laea = {


/* Initialize the Lambert Azimuthal Equal Area projection
  ------------------------------------------------------*/
  init: function() {
    this.sin_lat_o=Math.sin(this.lat0);
    this.cos_lat_o=Math.cos(this.lat0);
  },

/* Lambert Azimuthal Equal Area forward equations--mapping lat,long to x,y
  -----------------------------------------------------------------------*/
  forward: function(p) {

    /* Forward equations
      -----------------*/
    var lon=p.x;
    var lat=p.y;
    var delta_lon = Proj4js.common.adjust_lon(lon - this.long0);

    //v 1.0
    var sin_lat=Math.sin(lat);
    var cos_lat=Math.cos(lat);

    var sin_delta_lon=Math.sin(delta_lon);
    var cos_delta_lon=Math.cos(delta_lon);

    var g =this.sin_lat_o * sin_lat +this.cos_lat_o * cos_lat * cos_delta_lon;
    if (g == -1.0) {
      Proj4js.reportError("laea:fwd:Point projects to a circle of radius "+ 2.0 * R);
      return null;
    }
    var ksp = this.a * Math.sqrt(2.0 / (1.0 + g));
    var x = ksp * cos_lat * sin_delta_lon + this.x0;
    var y = ksp * (this.cos_lat_o * sin_lat - this.sin_lat_o * cos_lat * cos_delta_lon) + this.x0;
    p.x = x;
    p.y = y
    return p;
  },//lamazFwd()

/* Inverse equations
  -----------------*/
  inverse: function(p) {
    p.x -= this.x0;
    p.y -= this.y0;

    var Rh = Math.sqrt(p.x *p.x +p.y * p.y);
    var temp = Rh / (2.0 * this.a);

    if (temp > 1) {
      Proj4js.reportError("laea:Inv:DataError");
      return null;
    }

    var z = 2.0 * Proj4js.common.asinz(temp);
    var sin_z=Math.sin(z);
    var cos_z=Math.cos(z);

    var lon =this.long0;
    if (Math.abs(Rh) > Proj4js.common.EPSLN) {
       var lat = Proj4js.common.asinz(this.sin_lat_o * cos_z +this. cos_lat_o * sin_z *p.y / Rh);
       var temp =Math.abs(this.lat0) - Proj4js.common.HALF_PI;
       if (Math.abs(temp) > Proj4js.common.EPSLN) {
          temp = cos_z -this.sin_lat_o * Math.sin(lat);
          if(temp!=0.0) lon=Proj4js.common.adjust_lon(this.long0+Math.atan2(p.x*sin_z*this.cos_lat_o,temp*Rh));
       } else if (this.lat0 < 0.0) {
          lon = Proj4js.common.adjust_lon(this.long0 - Math.atan2(-p.x,p.y));
       } else {
          lon = Proj4js.common.adjust_lon(this.long0 + Math.atan2(p.x, -p.y));
       }
    } else {
      lat = this.lat0;
    }
    //return(OK);
    p.x = lon;
    p.y = lat;
    return p;
  }//lamazInv()
};



/* ======================================================================
    projCode/aeqd.js
   ====================================================================== */

Proj4js.Proj.aeqd = {

  init : function() {
    this.sin_p12=Math.sin(this.lat0)
    this.cos_p12=Math.cos(this.lat0)
  },

  forward: function(p) {
    var lon=p.x;
    var lat=p.y;
    var ksp;

    var sinphi=Math.sin(p.y);
    var cosphi=Math.cos(p.y); 
    var dlon = Proj4js.common.adjust_lon(lon - this.long0);
    var coslon = Math.cos(dlon);
    var g = this.sin_p12 * sinphi + this.cos_p12 * cosphi * coslon;
    if (Math.abs(Math.abs(g) - 1.0) < Proj4js.common.EPSLN) {
       ksp = 1.0;
       if (g < 0.0) {
         Proj4js.reportError("aeqd:Fwd:PointError");
         return;
       }
    } else {
       var z = Math.acos(g);
       ksp = z/Math.sin(z);
    }
    p.x = this.x0 + this.a * ksp * cosphi * Math.sin(dlon);
    p.y = this.y0 + this.a * ksp * (this.cos_p12 * sinphi - this.sin_p12 * cosphi * coslon);
    return p;
  },

  inverse: function(p){
    p.x -= this.x0;
    p.y -= this.y0;

    var rh = Math.sqrt(p.x * p.x + p.y *p.y);
    if (rh > (2.0 * Proj4js.common.HALF_PI * this.a)) {
       Proj4js.reportError("aeqdInvDataError");
       return;
    }
    var z = rh / this.a;

    var sinz=Math.sin(z)
    var cosz=Math.cos(z)

    var lon = this.long0;
    var lat;
    if (Math.abs(rh) <= Proj4js.common.EPSLN) {
      lat = this.lat0;
    } else {
      lat = Proj4js.common.asinz(cosz * this.sin_p12 + (p.y * sinz * this.cos_p12) / rh);
      var con = Math.abs(this.lat0) - Proj4js.common.HALF_PI;
      if (Math.abs(con) <= Proj4js.common.EPSLN) {
        if (lat0 >= 0.0) {
          lon = Proj4js.common.adjust_lon(this.long0 + Math.atan2(p.x , -p.y));
        } else {
          lon = Proj4js.common.adjust_lon(this.long0 - Math.atan2(-p.x , p.y));
        }
      } else {
        con = cosz - this.sin_p12 * Math.sin(lat);
        if ((Math.abs(con) < Proj4js.common.EPSLN) && (Math.abs(p.x) < Proj4js.common.EPSLN)) {
           //no-op, just keep the lon value as is
        } else {
          var temp = Math.atan2((p.x * sinz * this.cos_p12), (con * rh));
          lon = Proj4js.common.adjust_lon(this.long0 + Math.atan2((p.x * sinz * this.cos_p12), (con * rh)));
        }
      }
    }

    p.x = lon;
    p.y = lat;
    return p;
  } 
};
/* ======================================================================
    projCode/moll.js
   ====================================================================== */

/*******************************************************************************
NAME                            MOLLWEIDE

PURPOSE:  Transforms input longitude and latitude to Easting and
    Northing for the MOllweide projection.  The
    longitude and latitude must be in radians.  The Easting
    and Northing values will be returned in meters.

PROGRAMMER              DATE
----------              ----
D. Steinwand, EROS      May, 1991;  Updated Sept, 1992; Updated Feb, 1993
S. Nelson, EDC    Jun, 2993;  Made corrections in precision and
          number of iterations.

ALGORITHM REFERENCES

1.  Snyder, John P. and Voxland, Philip M., "An Album of Map Projections",
    U.S. Geological Survey Professional Paper 1453 , United State Government
    Printing Office, Washington D.C., 1989.

2.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.
*******************************************************************************/

Proj4js.Proj.moll = {

  /* Initialize the Mollweide projection
    ------------------------------------*/
  init: function(){
    //no-op
  },

  /* Mollweide forward equations--mapping lat,long to x,y
    ----------------------------------------------------*/
  forward: function(p) {

    /* Forward equations
      -----------------*/
    var lon=p.x;
    var lat=p.y;

    var delta_lon = Proj4js.common.adjust_lon(lon - this.long0);
    var theta = lat;
    var con = Proj4js.common.PI * Math.sin(lat);

    /* Iterate using the Newton-Raphson method to find theta
      -----------------------------------------------------*/
    for (var i=0;;i++) {
       var delta_theta = -(theta + Math.sin(theta) - con)/ (1.0 + Math.cos(theta));
       theta += delta_theta;
       if (Math.abs(delta_theta) < Proj4js.common.EPSLN) break;
       if (i >= 50) {
          Proj4js.reportError("moll:Fwd:IterationError");
         //return(241);
       }
    }
    theta /= 2.0;

    /* If the latitude is 90 deg, force the x coordinate to be "0 + false easting"
       this is done here because of precision problems with "cos(theta)"
       --------------------------------------------------------------------------*/
    if (Proj4js.common.PI/2 - Math.abs(lat) < Proj4js.common.EPSLN) delta_lon =0;
    var x = 0.900316316158 * this.R * delta_lon * Math.cos(theta) + this.x0;
    var y = 1.4142135623731 * this.R * Math.sin(theta) + this.y0;

    p.x=x;
    p.y=y;
    return p;
  },

  inverse: function(p){
    var theta;
    var arg;

    /* Inverse equations
      -----------------*/
    p.x-= this.x0;
    //~ p.y -= this.y0;
    var arg = p.y /  (1.4142135623731 * this.R);

    /* Because of division by zero problems, 'arg' can not be 1.0.  Therefore
       a number very close to one is used instead.
       -------------------------------------------------------------------*/
    if(Math.abs(arg) > 0.999999999999) arg=0.999999999999;
    var theta =Math.asin(arg);
    var lon = Proj4js.common.adjust_lon(this.long0 + (p.x / (0.900316316158 * this.R * Math.cos(theta))));
    if(lon < (-Proj4js.common.PI)) lon= -Proj4js.common.PI;
    if(lon > Proj4js.common.PI) lon= Proj4js.common.PI;
    arg = (2.0 * theta + Math.sin(2.0 * theta)) / Proj4js.common.PI;
    if(Math.abs(arg) > 1.0)arg=1.0;
    var lat = Math.asin(arg);
    //return(OK);

    p.x=lon;
    p.y=lat;
    return p;
  }
};

