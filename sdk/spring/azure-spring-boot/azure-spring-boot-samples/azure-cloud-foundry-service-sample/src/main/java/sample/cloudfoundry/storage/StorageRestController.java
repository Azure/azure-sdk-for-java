/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package sample.cloudfoundry.storage;

import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.TransferManager;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.AsynchronousFileChannel;

@SuppressFBWarnings({"RV_RETURN_VALUE_IGNORED"})
@RestController
public class StorageRestController {

    public static final String IMAGE_PATH =
            "https://raw.githubusercontent.com/mjeffries-pivotal/pcf-samples/master/images/azure-pcf.jpg";
    private static final Logger LOG = LoggerFactory
            .getLogger(StorageRestController.class);

    @Autowired
    private ContainerURL containerURL;

    @RequestMapping(value = "/blob", method = RequestMethod.GET)
    @ResponseBody
    public void showBlob(HttpServletResponse response) {
        InputStream is = null;

        try {
            LOG.info("showBlob start");
            if (containerURL == null) {
                LOG.error("ContainerURL is null!");
                return;
            }

            final URL u = new URL(IMAGE_PATH);
            is = u.openStream();
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            IOUtils.copy(is, response.getOutputStream());

            // Create container.
            containerURL.create(null, null, null);

            // Upload an image file.
            LOG.debug("Uploading image...");
            final BlockBlobURL blockBlobURL = containerURL.createBlockBlobURL("image1.jpg");
            final File imageFile = new File(IMAGE_PATH);
            final AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(imageFile.toPath());

            TransferManager.uploadFileToBlockBlob(fileChannel, blockBlobURL, 8 * 1024 * 1024, null)
                    .subscribe(r -> {
                        LOG.debug("Uploading image complete");
                    }, error -> {
                        LOG.error("Failed to upload image", error);
                    });

        } catch (IOException e) {
            LOG.error("Error retrieving image", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOG.warn("Failed to close the InputStream.", e);
                }
            }
        }
    }
}
