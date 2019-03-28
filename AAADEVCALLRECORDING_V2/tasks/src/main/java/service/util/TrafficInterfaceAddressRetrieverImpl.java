package service.util;

import com.avaya.asm.datamgr.AssetDM;
import com.avaya.asm.datamgr.DMFactory;
import com.avaya.collaboration.util.logger.Logger;

/**
 * EDP contains two network interface one for management (eth0) and another for services (eth1). For any traffic the snap-ins should be
 * using the trafficInterface (eth1) This class can be used to find the traffic interface address *
 */
public final class TrafficInterfaceAddressRetrieverImpl implements TrafficInterfaceAddressRetriever
{
    private final AssetDM assetDM;

    private final Logger logger;

    public TrafficInterfaceAddressRetrieverImpl()
    {
        this((AssetDM) DMFactory.getInstance().getDataMgr(AssetDM.class),
                Logger.getLogger(TrafficInterfaceAddressRetriever.class));
    }

    TrafficInterfaceAddressRetrieverImpl(final AssetDM assetDM, final Logger logger)
    {
        this.assetDM = assetDM;

        this.logger = logger;
    }

    @Override
    public String getTrafficInterfaceAddress()
    {
        final String localAsset = assetDM.getMyAssetIp();

        if (logger.isFinestEnabled())
        {
            logger.finest("getTrafficInterfaceAddress: entity ip addr=" + localAsset);
        }

        return localAsset;
    }
}
