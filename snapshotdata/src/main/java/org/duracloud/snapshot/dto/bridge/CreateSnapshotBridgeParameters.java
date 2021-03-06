/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import java.io.IOException;
import javax.xml.bind.annotation.XmlValue;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.BaseDTO;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Bill Branan
 * Date: 7/24/14
 */
public class CreateSnapshotBridgeParameters extends BaseDTO {

    /**
     * The host name of the DuraCloud instance
     */
    @XmlValue
    private String host;

    /**
     * The port on which DuraCloud is available
     */
    @XmlValue
    private String port;

    /**
     * The ID of the storage provider where the snapshot space can be found
     */
    @XmlValue
    private String storeId;

    /**
     * The ID of the space in which the content to snapshot resides
     */
    @XmlValue
    private String spaceId;

    /**
     * User-supplied description of the snapshot
     */
    @XmlValue
    private String description;

    /**
     * The Preservation Network Member UUID of associated with the storage provider account.
     */
    @XmlValue
    private String memberId;

    /**
     * The email address of the user, will be used for snapshot notifications
     */
    @XmlValue
    private String userEmail;

    public CreateSnapshotBridgeParameters() {
    }

    public CreateSnapshotBridgeParameters(String host,
                                          String port,
                                          String storeId,
                                          String spaceId,
                                          String description,
                                          String userEmail,
                                          String memberId) {
        this.host = host;
        this.port = port;
        this.storeId = storeId;
        this.spaceId = spaceId;
        this.description = description;
        this.userEmail = userEmail;
        this.memberId = memberId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    /**
     * Creates a serialized version of bridge parameters
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<CreateSnapshotBridgeParameters> serializer =
            new JaxbJsonSerializer<>(CreateSnapshotBridgeParameters.class);
        try {
            return serializer.serialize(this);
        } catch (IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
