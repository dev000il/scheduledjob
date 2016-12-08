package cn.rfidcn.scheduledjob.ip;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.Pattern;

/**
 * IP to location address
 *
 * @author senhui.li
 */
@SuppressWarnings("unused")
public class IP2Location {

    private static final Pattern pattern = Pattern
            .compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    private static final Pattern pattern2 = Pattern.compile("^([0-9A-F]{1,4}:){6}(0[0-9]+\\.|.*?\\.0[0-9]+).*$", 2);
    private static final Pattern pattern3 = Pattern.compile("^[0-9]+$");
    private static final Pattern pattern4 = Pattern.compile("^(.*:)(([0-9]+\\.){3}[0-9]+)$");
    private static final Pattern pattern5 = Pattern.compile("^.*((:[0-9A-F]{1,4}){2})$");
    private static final Pattern pattern6 = Pattern.compile("^[0:]+((:[0-9A-F]{1,4}){1,2})$", 2);
    private static final BigInteger MAX_IPV4_RANGE = new BigInteger("4294967295");
    private static final BigInteger MAX_IPV6_RANGE = new BigInteger("340282366920938463463374607431768211455");
    private static final int[] COUNTRY_POSITION = { 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 };
    private static final int[] REGION_POSITION = { 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 };
    private static final int[] CITY_POSITION = { 0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4 };
    private static final int[] ISP_POSITION = { 0, 0, 3, 0, 5, 0, 7, 5, 7, 0, 8, 0, 9, 0, 9, 0, 9, 0, 9, 7, 9, 0, 9, 7, 9 };
    private static final int[] LATITUDE_POSITION = { 0, 0, 0, 0, 0, 5, 5, 0, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 };
    private static final int[] LONGITUDE_POSITION = { 0, 0, 0, 0, 0, 6, 6, 0, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 };
    private static final int[] DOMAIN_POSITION = { 0, 0, 0, 0, 0, 0, 0, 6, 8, 0, 9, 0, 10, 0, 10, 0, 10, 0, 10, 8, 10, 0, 10, 8,
            10 };
    private static final int[] ZIPCODE_POSITION = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 7, 7, 7, 0, 7, 7, 7, 0, 7, 0, 7, 7, 7, 0, 7 };
    private static final int[] TIMEZONE_POSITION = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 8, 7, 8, 8, 8, 7, 8, 0, 8, 8, 8, 0, 8 };
    private static final int[] NETSPEED_POSITION = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 11, 0, 11, 8, 11, 0, 11, 0, 11, 0,
            11 };
    private static final int[] IDDCODE_POSITION = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 12, 0, 12, 0, 12, 9, 12, 0,
            12 };
    private static final int[] AREACODE_POSITION = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 13, 0, 13, 0, 13, 10, 13,
            0, 13 };
    private static final int[] WEATHERSTATIONCODE_POSITION = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 14, 0, 14,
            0, 14, 0, 14 };
    private static final int[] WEATHERSTATIONNAME_POSITION = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 15, 0, 15,
            0, 15, 0, 15 };
    private static final int[] MCC_POSITION = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 16, 0, 16, 9, 16 };
    private static final int[] MNC_POSITION = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 17, 0, 17, 10, 17 };
    private static final int[] MOBILEBRAND_POSITION = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 18, 0, 18,
            11, 18 };
    private static final int[] ELEVATION_POSITION = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 19, 0,
            19 };
    private static final int[] USAGETYPE_POSITION = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 20 };
    private MetaData _MetaData = null;
    private MappedByteBuffer _IPv4Buffer = null;
    private MappedByteBuffer _IPv6Buffer = null;
    private MappedByteBuffer _MapDataBuffer = null;
    private int _IPv4Offset = 0;
    private int _IPv6Offset = 0;
    private int _MapDataOffset = 0;
    private int _IPv4ColumnSize = 0;
    private int _IPv6ColumnSize = 0;
    public boolean UseMemoryMappedFile = false;
    public String IPDatabasePath = "";
    public String IPLicensePath = "";
    private boolean gotdelay = false;
    private boolean isdelayed = false;
    private boolean _alreadyCheckedKey = false;
    private int COUNTRY_POSITION_OFFSET;
    private int REGION_POSITION_OFFSET;
    private int CITY_POSITION_OFFSET;
    private int ISP_POSITION_OFFSET;
    private int DOMAIN_POSITION_OFFSET;
    private int ZIPCODE_POSITION_OFFSET;
    private int LATITUDE_POSITION_OFFSET;
    private int LONGITUDE_POSITION_OFFSET;
    private int TIMEZONE_POSITION_OFFSET;
    private int NETSPEED_POSITION_OFFSET;
    private int IDDCODE_POSITION_OFFSET;
    private int AREACODE_POSITION_OFFSET;
    private int WEATHERSTATIONCODE_POSITION_OFFSET;
    private int WEATHERSTATIONNAME_POSITION_OFFSET;
    private int MCC_POSITION_OFFSET;
    private int MNC_POSITION_OFFSET;
    private int MOBILEBRAND_POSITION_OFFSET;
    private int ELEVATION_POSITION_OFFSET;
    private int USAGETYPE_POSITION_OFFSET;
    private boolean COUNTRY_ENABLED;
    private boolean REGION_ENABLED;
    private boolean CITY_ENABLED;
    private boolean ISP_ENABLED;
    private boolean LATITUDE_ENABLED;
    private boolean LONGITUDE_ENABLED;
    private boolean DOMAIN_ENABLED;
    private boolean ZIPCODE_ENABLED;
    private boolean TIMEZONE_ENABLED;
    private boolean NETSPEED_ENABLED;
    private boolean IDDCODE_ENABLED;
    private boolean AREACODE_ENABLED;
    private boolean WEATHERSTATIONCODE_ENABLED;
    private boolean WEATHERSTATIONNAME_ENABLED;
    private boolean MCC_ENABLED;
    private boolean MNC_ENABLED;
    private boolean MOBILEBRAND_ENABLED;
    private boolean ELEVATION_ENABLED;
    private boolean USAGETYPE_ENABLED;

    private void DestroyMappedBytes() {
        if (this._IPv4Buffer != null) {
            this._IPv4Buffer = null;
        }
        if (this._IPv6Buffer != null) {
            this._IPv6Buffer = null;
        }
        if (this._MapDataBuffer != null) {
            this._MapDataBuffer = null;
        }
    }

    private void CreateMappedBytes() throws FileNotFoundException, IOException {
        RandomAccessFile localRandomAccessFile = null;
        try {
            localRandomAccessFile = new RandomAccessFile(this.IPDatabasePath, "r");
            FileChannel localFileChannel = localRandomAccessFile.getChannel();
            int i;
            if (this._IPv4Buffer == null) {
                i = this._IPv4ColumnSize * this._MetaData.getDBCount();
                this._IPv4Offset = (this._MetaData.getBaseAddr() - 1);
                this._IPv4Buffer = localFileChannel.map(FileChannel.MapMode.READ_ONLY, this._IPv4Offset, i);
                this._IPv4Buffer.order(ByteOrder.LITTLE_ENDIAN);
                this._MapDataOffset = (this._IPv4Offset + i);
            }
            if ((!this._MetaData.getOldBIN()) && (this._IPv6Buffer == null)) {
                i = this._IPv6ColumnSize * this._MetaData.getDBCountIPv6();
                this._IPv6Offset = (this._MetaData.getBaseAddrIPv6() - 1);
                this._IPv6Buffer = localFileChannel.map(FileChannel.MapMode.READ_ONLY, this._IPv6Offset, i);
                this._IPv6Buffer.order(ByteOrder.LITTLE_ENDIAN);
                this._MapDataOffset = (this._IPv6Offset + i);
            }
            if (this._MapDataBuffer == null) {
                this._MapDataBuffer = localFileChannel.map(FileChannel.MapMode.READ_ONLY, this._MapDataOffset,
                        localFileChannel.size() - this._MapDataOffset);
                this._MapDataBuffer.order(ByteOrder.LITTLE_ENDIAN);
            }
        } catch (FileNotFoundException localFileNotFoundException) {
            throw localFileNotFoundException;
        } catch (IOException localIOException) {
            throw localIOException;
        } finally {
            if (localRandomAccessFile != null) {
                localRandomAccessFile.close();
                localRandomAccessFile = null;
            }
        }
    }

    private boolean LoadBIN() throws Exception {
        boolean bool = false;
        RandomAccessFile localRandomAccessFile = null;
        try {
            if (this.IPDatabasePath.length() > 0) {
                localRandomAccessFile = new RandomAccessFile(this.IPDatabasePath, "r");
                FileChannel localFileChannel = localRandomAccessFile.getChannel();
                MappedByteBuffer localMappedByteBuffer = localFileChannel.map(FileChannel.MapMode.READ_ONLY, 0L, 64L);
                localMappedByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

                this._MetaData = new MetaData();

                this._MetaData.setDBType(localMappedByteBuffer.get(0));
                this._MetaData.setDBColumn(localMappedByteBuffer.get(1));
                this._MetaData.setDBYear(localMappedByteBuffer.get(2));
                this._MetaData.setDBMonth(localMappedByteBuffer.get(3));
                this._MetaData.setDBDay(localMappedByteBuffer.get(4));
                this._MetaData.setDBCount(localMappedByteBuffer.getInt(5));
                this._MetaData.setBaseAddr(localMappedByteBuffer.getInt(9));
                this._MetaData.setDBCountIPv6(localMappedByteBuffer.getInt(13));
                this._MetaData.setBaseAddrIPv6(localMappedByteBuffer.getInt(17));
                if (this._MetaData.getDBCountIPv6() == 0) {
                    this._MetaData.setOldBIN(true);
                }
                int i = this._MetaData.getDBColumn();
                this._IPv4ColumnSize = (i << 2);
                this._IPv6ColumnSize = (16 + (i - 1 << 2));

                int j = this._MetaData.getDBType();

                this.COUNTRY_POSITION_OFFSET = (COUNTRY_POSITION[j] != 0 ? COUNTRY_POSITION[j] - 1 << 2 : 0);
                this.REGION_POSITION_OFFSET = (REGION_POSITION[j] != 0 ? REGION_POSITION[j] - 1 << 2 : 0);
                this.CITY_POSITION_OFFSET = (CITY_POSITION[j] != 0 ? CITY_POSITION[j] - 1 << 2 : 0);
                this.ISP_POSITION_OFFSET = (ISP_POSITION[j] != 0 ? ISP_POSITION[j] - 1 << 2 : 0);
                this.DOMAIN_POSITION_OFFSET = (DOMAIN_POSITION[j] != 0 ? DOMAIN_POSITION[j] - 1 << 2 : 0);
                this.ZIPCODE_POSITION_OFFSET = (ZIPCODE_POSITION[j] != 0 ? ZIPCODE_POSITION[j] - 1 << 2 : 0);
                this.LATITUDE_POSITION_OFFSET = (LATITUDE_POSITION[j] != 0 ? LATITUDE_POSITION[j] - 1 << 2 : 0);
                this.LONGITUDE_POSITION_OFFSET = (LONGITUDE_POSITION[j] != 0 ? LONGITUDE_POSITION[j] - 1 << 2 : 0);
                this.TIMEZONE_POSITION_OFFSET = (TIMEZONE_POSITION[j] != 0 ? TIMEZONE_POSITION[j] - 1 << 2 : 0);
                this.NETSPEED_POSITION_OFFSET = (NETSPEED_POSITION[j] != 0 ? NETSPEED_POSITION[j] - 1 << 2 : 0);
                this.IDDCODE_POSITION_OFFSET = (IDDCODE_POSITION[j] != 0 ? IDDCODE_POSITION[j] - 1 << 2 : 0);
                this.AREACODE_POSITION_OFFSET = (AREACODE_POSITION[j] != 0 ? AREACODE_POSITION[j] - 1 << 2 : 0);
                this.WEATHERSTATIONCODE_POSITION_OFFSET = (WEATHERSTATIONCODE_POSITION[j] != 0 ? WEATHERSTATIONCODE_POSITION[j] - 1 << 2
                        : 0);
                this.WEATHERSTATIONNAME_POSITION_OFFSET = (WEATHERSTATIONNAME_POSITION[j] != 0 ? WEATHERSTATIONNAME_POSITION[j] - 1 << 2
                        : 0);
                this.MCC_POSITION_OFFSET = (MCC_POSITION[j] != 0 ? MCC_POSITION[j] - 1 << 2 : 0);
                this.MNC_POSITION_OFFSET = (MNC_POSITION[j] != 0 ? MNC_POSITION[j] - 1 << 2 : 0);
                this.MOBILEBRAND_POSITION_OFFSET = (MOBILEBRAND_POSITION[j] != 0 ? MOBILEBRAND_POSITION[j] - 1 << 2 : 0);
                this.ELEVATION_POSITION_OFFSET = (ELEVATION_POSITION[j] != 0 ? ELEVATION_POSITION[j] - 1 << 2 : 0);
                this.USAGETYPE_POSITION_OFFSET = (USAGETYPE_POSITION[j] != 0 ? USAGETYPE_POSITION[j] - 1 << 2 : 0);

                this.COUNTRY_ENABLED = (COUNTRY_POSITION[j] != 0);
                this.REGION_ENABLED = (REGION_POSITION[j] != 0);
                this.CITY_ENABLED = (CITY_POSITION[j] != 0);
                this.ISP_ENABLED = (ISP_POSITION[j] != 0);
                this.LATITUDE_ENABLED = (LATITUDE_POSITION[j] != 0);
                this.LONGITUDE_ENABLED = (LONGITUDE_POSITION[j] != 0);
                this.DOMAIN_ENABLED = (DOMAIN_POSITION[j] != 0);
                this.ZIPCODE_ENABLED = (ZIPCODE_POSITION[j] != 0);
                this.TIMEZONE_ENABLED = (TIMEZONE_POSITION[j] != 0);
                this.NETSPEED_ENABLED = (NETSPEED_POSITION[j] != 0);
                this.IDDCODE_ENABLED = (IDDCODE_POSITION[j] != 0);
                this.AREACODE_ENABLED = (AREACODE_POSITION[j] != 0);
                this.WEATHERSTATIONCODE_ENABLED = (WEATHERSTATIONCODE_POSITION[j] != 0);
                this.WEATHERSTATIONNAME_ENABLED = (WEATHERSTATIONNAME_POSITION[j] != 0);
                this.MCC_ENABLED = (MCC_POSITION[j] != 0);
                this.MNC_ENABLED = (MNC_POSITION[j] != 0);
                this.MOBILEBRAND_ENABLED = (MOBILEBRAND_POSITION[j] != 0);
                this.ELEVATION_ENABLED = (ELEVATION_POSITION[j] != 0);
                this.USAGETYPE_ENABLED = (USAGETYPE_POSITION[j] != 0);
                if (this.UseMemoryMappedFile) {
                    CreateMappedBytes();
                } else {
                    DestroyMappedBytes();
                }
                bool = true;
            }
        } catch (Exception localException) {
            throw localException;
        } finally {
            if (localRandomAccessFile != null) {
                localRandomAccessFile.close();
                localRandomAccessFile = null;
            }
        }
        return bool;
    }

    /**
     * @deprecated
     */
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public IPResult IPQuery(String paramString) throws Exception {
        paramString = paramString.trim();
        IPResult localIPResult1 = new IPResult(paramString);
        RandomAccessFile localRandomAccessFile = null;
        MappedByteBuffer localMappedByteBuffer = null;
        try {
            Object localObject1;
            if ((paramString == null) || (paramString.length() == 0)) {
                localIPResult1.status = "EMPTY_IP_ADDRESS";
                return localIPResult1;
            }
            int i = 0;
            int j = 0;
            int k = 0;
            int m = 0;
            int n = 0;
            int i1 = 0;
            int i2 = 0;
            BigInteger localBigInteger1 = BigInteger.ZERO;
            long l1 = 0L;
            long l2 = 0L;

            int i3 = 0;
            try {
                BigInteger[] arrayOfBigInteger = ip2no(paramString);
                j = arrayOfBigInteger[0].intValue();
                localObject1 = arrayOfBigInteger[1];
                i = arrayOfBigInteger[2].intValue();
                if (i == 6) {
                    String[] arrayOfString = ExpandIPv6(paramString, j);
                    localIPResult1.ip_address = arrayOfString[0];
                    j = Integer.parseInt(arrayOfString[1]);
                }
            } catch (UnknownHostException localUnknownHostException) {
                localIPResult1.status = "INVALID_IP_ADDRESS";
                return localIPResult1;
            }
            checkLicense();

            localIPResult1.delay = this.isdelayed;

            long l3 = 0L;
            long l4 = 0L;
            long l5 = 0L;
            long l6 = 0L;
            BigInteger localBigInteger2 = BigInteger.ZERO;
            BigInteger localBigInteger3 = BigInteger.ZERO;
            IPResult localIPResult3;
            if ((this._MetaData == null) && (!LoadBIN())) {
                localIPResult1.status = "MISSING_FILE";
                return localIPResult1;
            }
            k = this._MetaData.getDBType();
            n = this._MetaData.getDBColumn();
            if (this.UseMemoryMappedFile) {
                CreateMappedBytes();
            } else {
                DestroyMappedBytes();
                localRandomAccessFile = new RandomAccessFile(this.IPDatabasePath, "r");
                if (localRandomAccessFile == null) {
                    localIPResult1.status = "MISSING_FILE";
                    return localIPResult1;
                }
            }
            if (j == 4) {
                localBigInteger1 = MAX_IPV4_RANGE;
                l4 = this._MetaData.getDBCount();
                if (this.UseMemoryMappedFile) {
                    localMappedByteBuffer = this._IPv4Buffer;
                    i2 = localMappedByteBuffer.capacity();
                } else {
                    m = this._MetaData.getBaseAddr();
                }
                i1 = this._IPv4ColumnSize;
            } else {
                if (this._MetaData.getOldBIN()) {
                    localIPResult1.status = "IPV6_NOT_SUPPORTED";
                    return localIPResult1;
                }
                localBigInteger1 = MAX_IPV6_RANGE;
                l4 = this._MetaData.getDBCountIPv6();
                if (this.UseMemoryMappedFile) {
                    localMappedByteBuffer = this._IPv6Buffer;
                    i2 = localMappedByteBuffer.capacity();
                } else {
                    m = this._MetaData.getBaseAddrIPv6();
                }
                i1 = this._IPv6ColumnSize;
            }
            if (((BigInteger) localObject1).compareTo(localBigInteger1) == 0) {
                localObject1 = ((BigInteger) localObject1).subtract(BigInteger.ONE);
            }
            while (l3 <= l4) {
                l5 = (l3 + l4) / 2L;
                l1 = m + l5 * i1;
                l2 = l1 + i1;
                if (this.UseMemoryMappedFile) {
                    i3 = l2 >= i2 ? 1 : 0;
                }
                localBigInteger2 = read32or128(l1, j, localMappedByteBuffer, localRandomAccessFile);
                localBigInteger3 = i3 != 0 ? BigInteger.ZERO : read32or128(l2, j, localMappedByteBuffer, localRandomAccessFile);
                if ((((BigInteger) localObject1).compareTo(localBigInteger2) >= 0)
                        && (((BigInteger) localObject1).compareTo(localBigInteger3) < 0)) {
                    if (j == 6) {
                        l1 += 12L;
                    }
                    if (this.COUNTRY_ENABLED) {
                        l6 = read32(l1 + this.COUNTRY_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile).longValue();
                        localIPResult1.country_short = readStr(l6, localRandomAccessFile);
                        l6 += 3L;
                        localIPResult1.country_long = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.country_short = "Not_Supported";
                        localIPResult1.country_long = "Not_Supported";
                    }
                    if (this.REGION_ENABLED) {
                        l6 = read32(l1 + this.REGION_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile).longValue();
                        localIPResult1.region = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.region = "Not_Supported";
                    }
                    if (this.CITY_ENABLED) {
                        l6 = read32(l1 + this.CITY_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile).longValue();
                        localIPResult1.city = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.city = "Not_Supported";
                    }
                    if (this.ISP_ENABLED) {
                        l6 = read32(l1 + this.ISP_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile).longValue();
                        localIPResult1.isp = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.isp = "Not_Supported";
                    }
                    if (this.LATITUDE_ENABLED) {
                        l6 = l1 + this.LATITUDE_POSITION_OFFSET;
                        localIPResult1.latitude = readFloat(l6, localMappedByteBuffer, localRandomAccessFile);
                    } else {
                        localIPResult1.latitude = 0.0F;
                    }
                    if (this.LONGITUDE_ENABLED) {
                        l6 = l1 + this.LONGITUDE_POSITION_OFFSET;
                        localIPResult1.longitude = readFloat(l6, localMappedByteBuffer, localRandomAccessFile);
                    } else {
                        localIPResult1.longitude = 0.0F;
                    }
                    if (this.DOMAIN_ENABLED) {
                        l6 = read32(l1 + this.DOMAIN_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile).longValue();
                        localIPResult1.domain = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.domain = "Not_Supported";
                    }
                    if (this.ZIPCODE_ENABLED) {
                        l6 = read32(l1 + this.ZIPCODE_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile).longValue();
                        localIPResult1.zipcode = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.zipcode = "Not_Supported";
                    }
                    if (this.TIMEZONE_ENABLED) {
                        l6 = read32(l1 + this.TIMEZONE_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile).longValue();
                        localIPResult1.timezone = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.timezone = "Not_Supported";
                    }
                    if (this.NETSPEED_ENABLED) {
                        l6 = read32(l1 + this.NETSPEED_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile).longValue();
                        localIPResult1.netspeed = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.netspeed = "Not_Supported";
                    }
                    if (this.IDDCODE_ENABLED) {
                        l6 = read32(l1 + this.IDDCODE_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile).longValue();
                        localIPResult1.iddcode = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.iddcode = "Not_Supported";
                    }
                    if (this.AREACODE_ENABLED) {
                        l6 = read32(l1 + this.AREACODE_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile).longValue();
                        localIPResult1.areacode = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.areacode = "Not_Supported";
                    }
                    if (this.WEATHERSTATIONCODE_ENABLED) {
                        l6 = read32(l1 + this.WEATHERSTATIONCODE_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile)
                                .longValue();
                        localIPResult1.weatherstationcode = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.weatherstationcode = "Not_Supported";
                    }
                    if (this.WEATHERSTATIONNAME_ENABLED) {
                        l6 = read32(l1 + this.WEATHERSTATIONNAME_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile)
                                .longValue();
                        localIPResult1.weatherstationname = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.weatherstationname = "Not_Supported";
                    }
                    if (this.MCC_ENABLED) {
                        l6 = read32(l1 + this.MCC_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile).longValue();
                        localIPResult1.mcc = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.mcc = "Not_Supported";
                    }
                    if (this.MNC_ENABLED) {
                        l6 = read32(l1 + this.MNC_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile).longValue();
                        localIPResult1.mnc = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.mnc = "Not_Supported";
                    }
                    if (this.MOBILEBRAND_ENABLED) {
                        l6 = read32(l1 + this.MOBILEBRAND_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile)
                                .longValue();
                        localIPResult1.mobilebrand = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.mobilebrand = "Not_Supported";
                    }
                    if (this.ELEVATION_ENABLED) {
                        l6 = read32(l1 + this.ELEVATION_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile)
                                .longValue();
                        localIPResult1.elevation = convertFloat(readStr(l6, localRandomAccessFile));
                    } else {
                        localIPResult1.elevation = 0.0F;
                    }
                    if (this.USAGETYPE_ENABLED) {
                        l6 = read32(l1 + this.USAGETYPE_POSITION_OFFSET, localMappedByteBuffer, localRandomAccessFile)
                                .longValue();
                        localIPResult1.usagetype = readStr(l6, localRandomAccessFile);
                    } else {
                        localIPResult1.usagetype = "Not_Supported";
                    }
                    localIPResult1.status = "OK";
                    break;
                }
                if (((BigInteger) localObject1).compareTo(localBigInteger2) < 0) {
                    l4 = l5 - 1L;
                } else {
                    l3 = l5 + 1L;
                }
            }
            return localIPResult1;
        } catch (Exception localException) {
            localException.printStackTrace(System.out);
            throw localException;
        } finally {
            if (localRandomAccessFile != null) {
                localRandomAccessFile.close();
                localRandomAccessFile = null;
            }
        }
    }

    private String[] ExpandIPv6(String paramString, int paramInt) {
        /*
         * String str1 = paramString.toUpperCase(); String str2 =
         * String.valueOf(paramInt); Object localObject2; Object localObject3;
         * Object localObject4; if (paramInt == 4) { if
         * (pattern4.matcher(str1).matches()) { str1 = str1.replaceAll("::",
         * "0000:0000:0000:0000:0000:"); } else { localObject1 =
         * pattern5.matcher(str1); if (((Matcher)localObject1).matches()) {
         * localObject2 = ((Matcher)localObject1).group(1); localObject3 =
         * ((String)localObject2).replaceAll("^:+", "").replaceAll(":+$",
         * "").split(":"); int i = localObject3.length; localObject4 = new
         * StringBuffer(32); for (int k = 0; k < i; k++) { String str3 =
         * localObject3[k];
         * ((StringBuffer)localObject4).append("0000".substring(str3.length()) +
         * str3); } long l1 = new
         * BigInteger(((StringBuffer)localObject4).toString(), 16).longValue();
         * long[] arrayOfLong = { 0L, 0L, 0L, 0L }; for (int i3 = 0; i3 < 4;
         * i3++) { arrayOfLong[i3] = (l1 & 0xFF); l1 >>= 8; } str1 =
         * str1.replaceAll((String)localObject2 + "$", ":" + arrayOfLong[3] +
         * "." + arrayOfLong[2] + "." + arrayOfLong[1] + "." + arrayOfLong[0]);
         * str1 = str1.replaceAll("::", "0000:0000:0000:0000:0000:"); } } } else
         * if (paramInt == 6) { if (str1.equals("::")) { str1 = str1 +
         * "0.0.0.0"; str1 = str1.replaceAll("::",
         * "0000:0000:0000:0000:0000:FFFF:"); str2 = "4"; } else { localObject1
         * = pattern4.matcher(str1); String[] arrayOfString1; int n; Object
         * localObject5; if (((Matcher)localObject1).matches()) { localObject2 =
         * ((Matcher)localObject1).group(1); localObject3 =
         * ((Matcher)localObject1).group(2); arrayOfString1 =
         * ((String)localObject3).split("\\."); localObject4 = new int[4]; int m
         * = localObject4.length; for (n = 0; n < m; n++) { localObject4[n] =
         * Integer.parseInt(arrayOfString1[n]); } n = (localObject4[0] << 8) +
         * localObject4[1]; int i1 = (localObject4[2] << 8) + localObject4[3];
         * localObject5 = Integer.toHexString(n); String str5 =
         * Integer.toHexString(i1); StringBuffer localStringBuffer4 = new
         * StringBuffer(((String)localObject2).length() + 9);
         * localStringBuffer4.append((String)localObject2);
         * localStringBuffer4.append
         * ("0000".substring(((String)localObject5).length()));
         * localStringBuffer4.append((String)localObject5);
         * localStringBuffer4.append(":");
         * localStringBuffer4.append("0000".substring(str5.length()));
         * localStringBuffer4.append(str5); str1 =
         * localStringBuffer4.toString().toUpperCase(); String[] arrayOfString3
         * = str1.split("::"); String[] arrayOfString4 =
         * arrayOfString3[0].split(":"); StringBuffer localStringBuffer5 = new
         * StringBuffer(40); StringBuffer localStringBuffer6 = new
         * StringBuffer(40); StringBuffer localStringBuffer7 = new
         * StringBuffer(40); m = arrayOfString4.length; int i8 = 0; for (int i9
         * = 0; i9 < m; i9++) { if (arrayOfString4[i9].length() > 0) { i8++;
         * localStringBuffer5
         * .append("0000".substring(arrayOfString4[i9].length()));
         * localStringBuffer5.append(arrayOfString4[i9]);
         * localStringBuffer5.append(":"); } } int i11; if
         * (arrayOfString3.length > 1) { String[] arrayOfString5 =
         * arrayOfString3[1].split(":"); m = arrayOfString5.length; for (i11 =
         * 0; i11 < m; i11++) { if (arrayOfString5[i11].length() > 0) { i8++;
         * localStringBuffer6
         * .append("0000".substring(arrayOfString5[i11].length()));
         * localStringBuffer6.append(arrayOfString5[i11]);
         * localStringBuffer6.append(":"); } } } int i10 = 8 - i8; if (i10 == 6)
         * { for (i11 = 1; i11 < i10; i11++) {
         * localStringBuffer7.append("0000"); localStringBuffer7.append(":"); }
         * localStringBuffer7.append("FFFF:");
         * localStringBuffer7.append((String)localObject3); str2 = "4"; str1 =
         * localStringBuffer7.toString(); } else { for (i11 = 0; i11 < i10;
         * i11++) { localStringBuffer7.append("0000");
         * localStringBuffer7.append(":"); }
         * localStringBuffer5.append(localStringBuffer7
         * ).append(localStringBuffer6); str1 =
         * localStringBuffer5.toString().replaceAll(":$", ""); } } else {
         * localObject2 = pattern6.matcher(str1); StringBuffer
         * localStringBuffer2; int i5; if (((Matcher)localObject2).matches()) {
         * localObject3 = ((Matcher)localObject2).group(1); arrayOfString1 =
         * ((String)localObject3).replaceAll("^:+", "").replaceAll(":+$",
         * "").split(":"); int j = arrayOfString1.length; localStringBuffer2 =
         * new StringBuffer(32); for (n = 0; n < j; n++) { String str4 =
         * arrayOfString1[n];
         * localStringBuffer2.append("0000".substring(str4.length()) + str4); }
         * long l2 = new BigInteger(localStringBuffer2.toString(),
         * 16).longValue(); localObject5 = new long[] { 0L, 0L, 0L, 0L }; for
         * (i5 = 0; i5 < 4; i5++) { localObject5[i5] = (l2 & 0xFF); l2 >>= 8; }
         * str1 = str1.replaceAll((String)localObject3 + "$", ":" +
         * localObject5[3] + "." + localObject5[2] + "." + localObject5[1] + "."
         * + localObject5[0]); str1 = str1.replaceAll("::",
         * "0000:0000:0000:0000:0000:FFFF:"); str2 = "4"; } else { localObject3
         * = str1.split("::"); arrayOfString1 = localObject3[0].split(":");
         * StringBuffer localStringBuffer1 = new StringBuffer(40);
         * localStringBuffer2 = new StringBuffer(40); StringBuffer
         * localStringBuffer3 = new StringBuffer(40); int i2 =
         * arrayOfString1.length; int i4 = 0; for (i5 = 0; i5 < i2; i5++) { if
         * (arrayOfString1[i5].length() > 0) { i4++;
         * localStringBuffer1.append("0000"
         * .substring(arrayOfString1[i5].length()));
         * localStringBuffer1.append(arrayOfString1[i5]);
         * localStringBuffer1.append(":"); } } if (localObject3.length > 1) {
         * String[] arrayOfString2 = localObject3[1].split(":"); i2 =
         * arrayOfString2.length; for (i7 = 0; i7 < i2; i7++) { if
         * (arrayOfString2[i7].length() > 0) { i4++;
         * localStringBuffer2.append("0000"
         * .substring(arrayOfString2[i7].length()));
         * localStringBuffer2.append(arrayOfString2[i7]);
         * localStringBuffer2.append(":"); } } } int i6 = 8 - i4; for (int i7 =
         * 0; i7 < i6; i7++) { localStringBuffer3.append("0000");
         * localStringBuffer3.append(":"); }
         * localStringBuffer1.append(localStringBuffer3
         * ).append(localStringBuffer2); str1 =
         * localStringBuffer1.toString().replaceAll(":$", ""); } } } } Object
         * localObject1 = { str1, str2 }; return localObject1;
         */
        return null;
    }

    private float convertFloat(String paramString) {
        try {
            return Float.parseFloat(paramString);
        } catch (NumberFormatException localNumberFormatException) {
        }
        return 0.0F;
    }

    private void reverse(byte[] paramArrayOfByte) {
        if (paramArrayOfByte == null) {
            return;
        }
        int i = 0;
        int j = paramArrayOfByte.length - 1;
        while (j > i) {
            byte k = paramArrayOfByte[j];
            paramArrayOfByte[j] = paramArrayOfByte[i];
            paramArrayOfByte[i] = k;
            j--;
            i++;
        }
    }

    private BigInteger read32or128(long paramLong, int paramInt, MappedByteBuffer paramMappedByteBuffer,
            RandomAccessFile paramRandomAccessFile) throws IOException {
        if (paramInt == 4) {
            return read32(paramLong, paramMappedByteBuffer, paramRandomAccessFile);
        }
        if (paramInt == 6) {
            return read128(paramLong, paramMappedByteBuffer, paramRandomAccessFile);
        }
        return BigInteger.ZERO;
    }

    private BigInteger read128(long paramLong, MappedByteBuffer paramMappedByteBuffer, RandomAccessFile paramRandomAccessFile)
            throws IOException {
        BigInteger localBigInteger = BigInteger.ZERO;

        byte[] arrayOfByte = new byte[16];
        int i;
        if (this.UseMemoryMappedFile) {
            for (i = 0; i < 16; i++) {
                arrayOfByte[i] = paramMappedByteBuffer.get((int) paramLong + i);
            }
        } else {
            paramRandomAccessFile.seek(paramLong - 1L);
            for (i = 0; i < 16; i++) {
                arrayOfByte[i] = paramRandomAccessFile.readByte();
            }
        }
        reverse(arrayOfByte);
        localBigInteger = new BigInteger(1, arrayOfByte);
        return localBigInteger;
    }

    private BigInteger read32(long paramLong, MappedByteBuffer paramMappedByteBuffer, RandomAccessFile paramRandomAccessFile)
            throws IOException {
        if (this.UseMemoryMappedFile) {
            return BigInteger.valueOf(paramMappedByteBuffer.getInt((int) paramLong) & 0xFFFFFFFF);
        }
        paramRandomAccessFile.seek(paramLong - 1L);
        byte[] arrayOfByte = new byte[4];
        for (int i = 0; i < 4; i++) {
            arrayOfByte[i] = paramRandomAccessFile.readByte();
        }
        reverse(arrayOfByte);
        return new BigInteger(1, arrayOfByte);
    }

    private String readStr(long paramLong, RandomAccessFile paramRandomAccessFile) throws IOException {
        char[] arrayOfChar = null;
        int i;
        if (this.UseMemoryMappedFile) {
            paramLong -= this._MapDataOffset;
            i = this._MapDataBuffer.get((int) paramLong);
            try {
                arrayOfChar = new char[i];
                for (int j = 0; j < i; j++) {
                    arrayOfChar[j] = ((char) this._MapDataBuffer.get((int) paramLong + 1 + j));
                }
            } catch (NegativeArraySizeException localNegativeArraySizeException1) {
                return null;
            }
        } else {
            paramRandomAccessFile.seek(paramLong);
            i = paramRandomAccessFile.read();
            try {
                arrayOfChar = new char[i];
                for (int k = 0; k < i; k++) {
                    arrayOfChar[k] = ((char) paramRandomAccessFile.read());
                }
            } catch (NegativeArraySizeException localNegativeArraySizeException2) {
                return null;
            }
        }
        return String.copyValueOf(arrayOfChar);
    }

    private float readFloat(long paramLong, MappedByteBuffer paramMappedByteBuffer, RandomAccessFile paramRandomAccessFile)
            throws IOException {
        if (this.UseMemoryMappedFile) {
            return paramMappedByteBuffer.getFloat((int) paramLong);
        }
        paramRandomAccessFile.seek(paramLong - 1L);
        int[] arrayOfInt = new int[4];
        for (int i = 0; i < 4; i++) {
            arrayOfInt[i] = paramRandomAccessFile.read();
        }
        return Float.intBitsToFloat(arrayOfInt[3] << 24 | arrayOfInt[2] << 16 | arrayOfInt[1] << 8 | arrayOfInt[0]);
    }

    private BigInteger[] ip2no(String paramString) throws UnknownHostException {
        BigInteger localBigInteger = BigInteger.ZERO;
        BigInteger localBigInteger1 = BigInteger.ZERO;
        BigInteger localBigInteger2 = BigInteger.ZERO;
        BigInteger localBigInteger3 = new BigInteger("4");
        if (pattern.matcher(paramString).matches()) {
            localBigInteger1 = new BigInteger("4");
            localBigInteger2 = new BigInteger(String.valueOf(ipv4no(paramString)));
        } else {
            if ((pattern2.matcher(paramString).matches()) || (pattern3.matcher(paramString).matches())) {
                throw new UnknownHostException();
            }
            localBigInteger3 = new BigInteger("6");
            InetAddress localObject = InetAddress.getByName(paramString);
            byte[] arrayOfByte = ((InetAddress) localObject).getAddress();

            String str = "0";
            if ((localObject instanceof Inet6Address)) {
                str = "6";
            } else if ((localObject instanceof Inet4Address)) {
                str = "4";
            }
            localBigInteger1 = new BigInteger(str);
            localBigInteger2 = new BigInteger(1, arrayOfByte);
        }
        BigInteger[] localObject = { localBigInteger1, localBigInteger2, localBigInteger3 };

        return localObject;
    }

    private long ipv4no(String paramString) {
        String[] arrayOfString = paramString.split("\\.");
        long l1 = 0L;
        long l2 = 0L;
        for (int i = 3; i >= 0; i--) {
            l2 = Long.parseLong(arrayOfString[(3 - i)]);
            l1 |= l2 << (i << 3);
        }
        return l1;
    }

    private void checkLicense() {
        /*
         * if (!this._alreadyCheckedKey) { if ((this.IPLicensePath == null) ||
         * (this.IPLicensePath.length() == 0)) { this.IPLicensePath =
         * "license.key"; } String str1 = ""; String str2 = ""; String str3 =
         * ""; try { BufferedReader localBufferedReader = new BufferedReader(new
         * FileReader(this.IPLicensePath)); if (!localBufferedReader.ready()) {
         * localBufferedReader.close(); throw new IOException(); } if (((str1 =
         * localBufferedReader.readLine()) != null) && ((str2 =
         * localBufferedReader.readLine()) != null)) { str3 =
         * localBufferedReader.readLine(); } localBufferedReader.close(); if
         * ((str3 == null) || (str3.length() == 0)) { if
         * (!str2.trim().equals(generateKey(str1))) { this.gotdelay = true; } }
         * else if (!str3.trim().equals(generateKey(str1 + str2))) {
         * this.gotdelay = true; } this._alreadyCheckedKey = true; } catch
         * (IOException localIOException) { this.gotdelay = true; } }
         */
        this._alreadyCheckedKey = true;
        this.gotdelay = true;
    }

    private void delay() {
        if (this.gotdelay) {
            int i = 1 + (int) (Math.random() * 10.0D);
            try {
                if (i == 10) {
                    this.isdelayed = true;
                    Thread.sleep(5000L);
                } else {
                    this.isdelayed = false;
                }
            } catch (Exception localException) {
                System.out.println(localException);
            }
        }
    }

    private String generateKey(String paramString) {
        String[] arrayOfString1 = new String[2];
        String[] arrayOfString2 = new String[62];
        String[] arrayOfString3 = new String['È'];

        StringBuffer localStringBuffer = new StringBuffer(50);
        try {
            if (paramString.length() > 20) {
                arrayOfString1[0] = paramString.substring(1, 20);
            } else {
                arrayOfString1[0] = paramString;
            }
            arrayOfString1[1] = "Hexasoft";
            int i = 0;
            for (int j = 48; j <= 57; j++) {
                arrayOfString2[i] = String.valueOf((char) j);
                i++;
            }
            for (int j = 65; j <= 90; j++) {
                arrayOfString2[i] = String.valueOf((char) j);
                i++;
            }
            for (int j = 97; j <= 122; j++) {
                arrayOfString2[i] = String.valueOf((char) j);
                i++;
            }
            String[] arrayOfString4 = { String.valueOf(Asc("7")), String.valueOf(Asc("0")), String.valueOf(Asc("a")),
                    String.valueOf(Asc("1")), String.valueOf(Asc("b")), String.valueOf(Asc("2")), String.valueOf(Asc("c")),
                    String.valueOf(Asc("3")), String.valueOf(Asc("d")), String.valueOf(Asc("4")), String.valueOf(Asc("e")),
                    String.valueOf(Asc("5")), String.valueOf(Asc("f")), String.valueOf(Asc("6")), String.valueOf(Asc("g")) };
            for (int j = 0; j < 200; j++) {
                arrayOfString3[j] = arrayOfString4[(j % 15)];
            }
            i = 0;
            int n = 0;
            for (int k = 0; k <= 1; k++) {
                for (int m = 0; m <= 11; m++) {
                    n = arrayOfString1[k].length();
                    for (int j = 0; j < n; j++) {
                        int tmp461_460 = (i % 200);
                        String[] tmp461_453 = arrayOfString3;
                        tmp461_453[tmp461_460] = (tmp461_453[tmp461_460] + String.valueOf(Asc(arrayOfString1[k].substring(j,
                                j + 1))));
                        i++;
                    }
                    arrayOfString3[(i % 200)] = String.valueOf(Long.parseLong(arrayOfString3[(i % 200)]) >> 1);
                    i++;
                }
            }
            for (int k = 0; k <= 15; k++) {
                localStringBuffer.append(arrayOfString2[(Integer.parseInt(arrayOfString3[k]) % 62)]);
            }
        } catch (Exception localException) {
            System.out.println(localException);
        }
        if (localStringBuffer.length() == 0) {
            return "error";
        }
        for (int j = 0; j < 3; j++) {
            localStringBuffer.insert((j + 1 << 2) + j, '-');
        }
        return localStringBuffer.toString().toUpperCase();
    }

    private int Asc(String paramString) {
        try {
            String str1 = " !\"#$%&'()*+'-./0123456789:;<=>?@";
            String str2 = "abcdefghijklmnopqrstuvwxyz";
            str1 = str1 + str2.toUpperCase();
            str1 = str1 + "[\\]^_`";
            str1 = str1 + str2;
            str1 = str1 + "{|}~";

            int i = str1.indexOf(paramString);
            if (i > -1) {
                return i + 32;
            }
        } catch (Exception localException) {
            System.out.println(localException);
        }
        return 0;
    }
}
