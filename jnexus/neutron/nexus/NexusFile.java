/**
  *
  * The NeXus-API for Java. NeXus is an attempt to define a common data 
  * format for neutron and x-ray diffraction. NeXus is built on top of the
  * Hierarchical Data Format from NCSA. There exist already API's to
  * NeXus files for F77, F90, C and C++. This is an implementation of
  * a Java NeXus API using native methods.
  *
  * Some changes to the API have been necessary however, due to the 
  * different calling standards between C and Java. 
  *
  *
  * @author Mark Koennecke, October 2000
  *
  * copyright: see accompanying COPYRIGHT file
  */
package neutron.nexus;

import java.util.Hashtable;
import java.io.File;
import ncsa.hdf.hdflib.HDFArray;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFConstants;

public class  NexusFile implements NeXusFileInterface {

    // constants 
    /**
      * possible access codes, @see #NexusFile.
      */
    public final static int NXACC_READ = HDFConstants.DFACC_READ;
    public final static int NXACC_RDWR = HDFConstants.DFACC_RDWR;
    public final static int NXACC_CREATE = HDFConstants.DFACC_CREATE; 
    
    /**
      * constant denoting an unlimited dimension.
      */
    public final static int NX_UNLIMITED = 0;

    /**
      * constants for number types. @see #makedata, @see #putattr 
      * and others.
      */
    public final static int NX_FLOAT32 = HDFConstants.DFNT_FLOAT32; 
    public final static int NX_FLOAT64 = HDFConstants.DFNT_FLOAT64; 
    public final static int NX_INT8 = HDFConstants.DFNT_INT8; 
    public final static int NX_UINT8 = HDFConstants.DFNT_UINT8; 
    public final static int NX_INT16 = HDFConstants.DFNT_INT16; 
    public final static int NX_UINT16 = HDFConstants.DFNT_UINT16; 
    public final static int NX_INT32 = HDFConstants.DFNT_INT32; 
    public final static int NX_UINT32 = HDFConstants.DFNT_UINT32; 
    public final static int NX_CHAR   = HDFConstants.DFNT_CHAR8;

    /**
      * constants for compression schemes @see #compress
      */
    public final static int NX_COMP_NONE = HDFConstants.COMP_CODE_NONE;
    public final static int NX_COMP_LZW = HDFConstants.COMP_CODE_DEFLATE;
    public final static int NX_COMP_RLE = HDFConstants.COMP_CODE_RLE;
    public final static int NX_COMP_HUF = HDFConstants.COMP_CODE_SKPHUFF;

    /**
      * Maximum name length, must be VGNAMELENMAX in hlimits.h
      */
    protected final static int MAXNAMELEN = 64;

    /*
       This code takes care of loading the static library required for
       this class to work properly. The algorithm first looks for a 
       property neutron.nexus.JNEXUSLIB and loads that file if available,
       else it tries to locate the library in the system shared library 
       path.
    */
     static
     {
        String filename = null;   
        filename = System.getProperty("neutron.nexus.JNEXUSLIB",null);
        if ((filename != null) && (filename.length() > 0))
        {
            File hdfdll = new File(filename);
            if (hdfdll.exists() && hdfdll.canRead() && hdfdll.isFile()) 
	    {
                System.load(filename);
             } else {
                 throw (new UnsatisfiedLinkError("Invalid JNEXUS library"));
             }
         }
         else {
            System.loadLibrary("jnexus");
         }
      }

    /**
      * This is the handle to the NeXus file handle.
      */
    protected int handle;

    // Construction
    // native methods for this section
    protected native int  init(String filename, int access);
    protected native void close(int handle);
    protected native int  nxflush(int handle);

    /**
      * constructs a new NexusFile Object.
      * @param filename The name of the NeXus file to access.
      * @param access. The access mode for the file. Can only be one
      * of the predefined NeXus access code NXACC.... These are:
      * <dl>
      * <dt>NXACC_CREATE
      * <DD>or creating a new file.
      * <DT>NXACC_RDWR
      * <DD>For opening an existing file for modification or appending 
      * data.
      * <DT>NXACC_READ
      * <DD>For opening a file for reading.
      * </dl>
      * @exception NexusException when the file could not be found or
      * an HDF error occurred.
      */
    public NexusFile(String filename, int access) throws NexusException 
    {
         handle = init(filename,access);
    }
    /**
      * flushes all pending data to disk. Closes any open SDS's.
      */
    public void flush() throws NexusException
    {
        if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
	 handle = nxflush(handle);
    }
    /**
      * removes all NeXus file data structures and closes the file. This 
      * function should automatically be called by the Java garbage 
      * collector whenever the NexusFile object falls into disuse. However
      * the time when this is done is left to the garbage collector. My
      * personal experience is that finalize might never be called. I
      * suggest, to call finalize yourself when you are done with the 
      * NeXus file. finalize makes sure that multiple invocations will not
      * do any harm.
      */   
    public void finalize() throws Throwable 
    {
      if(handle  >= 0)
      {
         close(handle);
         handle = 0;
      }
    }


    // group functions
    //native methods for this section
    protected native void nxmakegroup(int handle, String name, String nxclass);
    protected native void nxopengroup(int handle, String name, String nxclass);
    protected native void nxclosegroup(int handle);

    /** 
      * makegroup creates a new group below the current group within
      * the NeXus file hierarchy.
      * @param name The name of the group to create.
      * @param nxclass The classname of the group.
      * @exception NexusException if an error occurs during this operation.
      */ 
    public void makegroup(String name, String nxclass) throws 
                            NexusException
    {
        if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
	nxmakegroup(handle, name, nxclass);
    }
    /**
      * opengroup opens the group name with class nxclass. 
      * The group must exist, otherwise an exception is thrown. opengroup is
      * similar to a cd name in a filesystem.
      * @param name the name of the group to open.
      * @param nxclass the classname of the group to open. 
      * @exception NexusException when something goes wrong.
      */   
    public void opengroup(String name, String nxclass) throws 
                             NexusException
    {
        if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
	nxopengroup(handle, name, nxclass);
    }
    /**
      * closegroup closes access to the current group and steps down one
      * step in group hierarchy.
      * @exception NexusException when an HDF error occurs during this
      * operation. 
      */
    public void closegroup() throws 
                             NexusException
    {
        if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
	nxclosegroup(handle);
    }

    // data set handling
    // native methods for this section
    protected native void nxmakedata(int handle, String name, int type,
                                  int rank, int dim[]);
    protected native void nxopendata(int handle, String name);
    protected native void nxclosedata(int handle);
    protected native void nxcompress(int handle, int compression_type);
    /**
      * makedata creates a new dataset with the specified characteristics 
      * in the current group.
      * @param name The name of the dataset.
      * @param type The number type of the dataset. Usually a constant from
      * a selection of values.
      * @param rank The rank or number of dimensions of the dataset.
      * @param dim An array containing the length of each dimension. dim must
      * have at least rank entries. The first dimension can be 0 which
      * means it is an unlimited dimension.
      * @exception NexusException when the dataset could not be created.
      */ 
    public void makedata(String name, int type, int rank, int dim[]) throws
	                   NexusException
    {
        if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
        checkType(type);
	nxmakedata(handle,name,type,rank,dim);
    }
    /**
      * opendata opens an existing dataset for access. For instance for 
      * reading or writing.
      * @param name The name of the dataset to open.
      * @exception NexusException when the dataset does not exist or 
      * something else is wrong.
      */
    public void  opendata(String name)throws 
                           NexusException
    {
        if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
	nxopendata(handle,name);
    }
    /**
      * closedata closes an opened dataset. Then no further access is 
      * possible without a call to opendata.
      * @exception NexusException when an HDF error occurrs.
      */
    public void closedata() throws
                           NexusException
    {
        if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
	nxclosedata(handle);
    }
    /**
      * causes the currently open dataset to be compressed on file.
      * This must be called after makedata and before writing to the
      * dataset.
      * @param compression_type determines the type of compression 
      * to use.
      * @exception NexusException when no dataset is open or an HDF error 
      * occurs.
      */ 
    public void compress(int compression_type) throws 
                           NexusException
    {
        if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
        switch(compression_type) {
	case NexusFile.NX_COMP_NONE:
	case NexusFile.NX_COMP_LZW:
	case NexusFile.NX_COMP_RLE:
	case NexusFile.NX_COMP_HUF:
	    break;
	default:
	    throw new NexusException("Invalid compression code requested");

	}
	nxcompress(handle,compression_type);
    }

    // data set reading
    // native methods in this section
    protected native void nxgetdata(int handle, byte bdata[]);
    protected native void nxgetslab(int handle, int Start[], int size[],
                                    byte bdata[]);
    protected native void nxgetattr(int handle, String name, byte bdata[],
                                    int args[]);
    /**
      * getdata reads the data from an previously openend dataset into
      * array.
      * @param array An n-dimensional array of the appropriate number
      * type for the dataset. Make sure to have the right type and size
      * here.
      * @exception NexusException when either an HDF error occurs or 
      * no dataset is open or array is not of the right type to hold
      * the data.
      */
    public void getdata(Object array)throws 
                          NexusException
    {
        byte bdata[];
        if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
        try{
	    HDFArray ha = new HDFArray(array);
            bdata = ha.emptyBytes();
            nxgetdata(handle,bdata);
            array = ha.arrayify(bdata);
	 }catch(HDFException he) {
           throw new NexusException(he.getMessage());
	 }
    }
    /**
      * getslab reads a subset of a large dataset into array.
      * @param start An array of dimension rank which contains the start 
      * position in the dataset from where to start reading.
      * @param size An array of dimension rank which contains the size 
      * of the dataset subset in each dimension to read.
      * @param array An array for holding the returned data values.
      * @exception NexusException when either an HDF error occurs or 
      * no dataset is open or array is not of the right type to hold
      * the data.
      */
    public void getslab(int start[], int size[],Object array)throws
                          NexusException
    {
        byte bdata[];
        if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
        try{
	    HDFArray ha = new HDFArray(array);
            bdata = ha.emptyBytes();
            nxgetslab(handle,start,size,bdata);
            array = ha.arrayify(bdata);
	 }catch(HDFException he) {
           throw new NexusException(he.getMessage());
	 }
    }
    /**
      * getattr retrieves the data associated with the attribute 
      * name. 
      * @param name The name of the attribute.
      * @param array an array with sufficient space for holding the attribute 
      * data.
      * @param args An integer array of dimension rank which holds the
      * length of the array as first value and the type as the last
      * value. Both values will be updated during reading.
      * @exception NexusException when either an HDF error occurs or 
      * the attribute could not be found.
      */
    public void getattr(String name,Object array, int args[])throws
                          NexusException
    {
        byte bdata[];
        if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
        checkType(args[1]);
        try{
	    HDFArray ha = new HDFArray(array);
            bdata = ha.emptyBytes();
            nxgetattr(handle, name, bdata,args);
            array = ha.arrayify(bdata);
	 }catch(HDFException he) {
           throw new NexusException(he.getMessage());
	 }
    }

    // data set writing
    // native methods for this section
    protected native void nxputdata(int handle, byte array[]); 
    protected native void nxputslab(int handle, byte array[], 
                                    int start[],int end[]); 
    protected native void nxputattr(int handle, String name,
                                    byte array[], int type); 

    /**
      * putdata writes the data from array into a previously opened
      * dataset.
      * @param array The data to write.
      * @exception NexusException when an HDF error occurs.
      */
    public void putdata(Object array) throws 
	                  NexusException
    {
       byte data[];

       if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
       try{
           HDFArray ha =  new HDFArray(array);
           data = ha.byteify();
           ha = null;
       }catch(HDFException he) {
	   throw new NexusException(he.getMessage());
       }
       nxputdata(handle,data);
       data = null;
    }
    /**
      * putslab writes a subset of a larger dataset to a previously opened
      * dataset.
      * @param array The data to write.
      * @param start An integer array of dimension rank which holds the
      * startcoordinates of the data subset in the larger dataset.
      * @param end An integer array of dimension rank which holds the
      * size in each dimension of the data subset to write.
      * @exception NexusException when an HDF error occurs.
      */ 
    public void putslab(Object array, int start[], int end[]) throws
                          NexusException
    {
       byte data[];

       if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
       try{
           HDFArray ha =  new HDFArray(array);
           data = ha.byteify();
           ha = null;
       }catch(HDFException he) {
	   throw new NexusException(he.getMessage());
       }
       nxputslab(handle,data,start,end);
       data = null;
    }
    /**
      * putattr adds a named attribute to a previously opened dataset or
      * a global attribute if no dataset is open.
      * @param name The name of the attribute.
      * @param array The data of the attribute.
      * @param iType The number type of the attribute.
      * @exception NexusException if an HDF error occurs.
      */  
    public void putattr(String name, Object array, int iType) throws
                          NexusException
    {
       byte data[];

       if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
       checkType(iType);
       try{
           HDFArray ha =  new HDFArray(array);
           data = ha.byteify();
           ha = null;
       }catch(HDFException he) {
	   throw new NexusException(he.getMessage());
       }
       nxputattr(handle,name,data,iType);
       data = null;
    }

    // inquiry
    //native methods for this section
    protected native void nxgetinfo(int handle, int iDim[], int args[]);
    protected native int nextentry(int handle, String names[]);
    protected native int nextattr(int handle, String names[], int args[]);
    /**
      * getinfo retrieves information about a previously opened dataset.
      * @param iDim An array which will be filled with the size of
      * the dataset in each dimension.
      * @param args An integer array which will hold more information about
      * the dataset after return. The fields: args[0] is the rank, args[1] is
      * the number type.
      * @exception NexusException when  an HDF error occurs.
      */ 
    public void getinfo(int iDim[], int args[]) throws 
                          NexusException
    {
       if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
       nxgetinfo(handle,iDim,args);
    }
    /**
      * groupdir will retrieve the content of the currently open vGroup.
      * groupdir is similar to an ls in unix. 
      * @return A Hashtable  which will hold the names of the items in 
      * the group as keys and the NeXus classname for vGroups or the 
      * string 'SDS' for datasets as values. 
      * @exception NexusException if an HDF error occurs
      */
    public Hashtable groupdir()throws
                          NexusException
    {
        if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
        Hashtable h = new Hashtable();
        String names[] = new String[2];
        int i;
        while(nextentry(handle,names) != -1)
	{
           h.put(names[0],names[1]);
        }
        return h;
    }
    /**
      * attrdir returns the attributes of the currently open dataset or
      * the file global attributes if no dataset is open.
      * @return A Hashtable which will hold the names of the attributes
      * as keys. For each key there is an AttributeEntry class as value.
      * @exception NexusException when an HDF error occurs.
      */ 
    public Hashtable attrdir()throws
                          NexusException
    {
        int args[] = new int[2];
        AttributeEntry at;
        String names[] = new String[1];

        Hashtable h = new Hashtable();
        if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
        while(nextattr(handle,names,args) != -1)
	{
          at = new AttributeEntry();
          at.length = args[0];
          at.type   = args[1];
          h.put(names[0], at);
        } 
        return h;
    }
    
    // linking 
    // native methods for this section
    protected native void nxgetgroupid(int handle, NXlink link);
    protected native void nxgetdataid(int handle, NXlink link);
    protected native void nxmakelink(int handle, NXlink target); 
    /**
      * getgroupID gets the data necessary for linking the current vGroup
      * somewhere else.
      * @return A NXlink class holding the link data.
      * @exception NexusException if an HDF error occurs.
      */
    public NXlink getgroupID() throws
                          NexusException
    {
      if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
      NXlink l = new NXlink();
      nxgetgroupid(handle,l);
      return l;
    }
    /**
      * getdataID gets the data necessary for linking the current dataset
      * somewhere else.
      * @return A NXlink class holding the link data.
      * @exception NexusException if an HDF error occurs.
      */
    public NXlink getdataID()throws
                          NexusException
    {
      if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
      NXlink l = new NXlink();
      nxgetdataid(handle,l);
      return l;
    }
    /**
      * makelink links the object described by target into the current
      * vGroup.
      * @param target The Object to link into the current group.
      * @exception NexusException if an error occurs.
      */
    public void   makelink(NXlink target)throws
                          NexusException
    {
      if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
      nxmakelink(handle,target);
    }

    /**
      * checkType verifies if a parameter is a valid NeXus type code. 
      * If not an exception is thrown.
      * @param type The type value to check.
      * @exception NexusException if the the type is no known type value
      */
    private void checkType(int type) throws NexusException
    {
	switch(type) {
	case NexusFile.NX_INT8:
	case NexusFile.NX_UINT8:
        case NexusFile.NX_CHAR:
	case NexusFile.NX_INT16:
	case NexusFile.NX_UINT16:
	case NexusFile.NX_INT32:
	case NexusFile.NX_UINT32:
	case NexusFile.NX_FLOAT32:
	case NexusFile.NX_FLOAT64:
	    break;
        default:
	    throw new NexusException("Illegal number type requested");
        }
    } 
    /**
      * debugstop is a debugging helper function which goes into an 
      * endless loop in the dynamic link library. Then a unix debugger
      * may attach to the running java process using the pid, interrupt,
      * set the loop variable to leave the loop, set a new breakpoint and
      * continue debugging. This works with ladebug on DU40D. This is an
      * developer support routine and should NEVER be called in normal
      * code. 
      */
    public native void debugstop();
}
  







