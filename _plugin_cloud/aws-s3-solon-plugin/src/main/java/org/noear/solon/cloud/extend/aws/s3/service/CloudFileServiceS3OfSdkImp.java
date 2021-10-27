package org.noear.solon.cloud.extend.aws.s3.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.noear.solon.Utils;
import org.noear.solon.cloud.exception.CloudFileException;
import org.noear.solon.cloud.extend.aws.s3.S3Props;
import org.noear.solon.cloud.model.Media;
import org.noear.solon.cloud.service.CloudFileService;
import org.noear.solon.core.handle.Result;

import java.util.Properties;

/**
 * 云端文件服务（aws s3）
 *
 * @author noear
 * @since 1.3
 */
public class CloudFileServiceS3OfSdkImp implements CloudFileService {
    private static CloudFileServiceS3OfSdkImp instance;

    public static synchronized CloudFileServiceS3OfSdkImp getInstance() {
        if (instance == null) {
            instance = new CloudFileServiceS3OfSdkImp();
        }

        return instance;
    }

    protected final String bucketDef;

    protected final String accessKey;
    protected final String secretKey;
    protected final String regionId;

    protected final AmazonS3 client;
    protected final AccessControlList acls = new AccessControlList();

    private CloudFileServiceS3OfSdkImp() {
        this(
                S3Props.instance.getFileRegionId(),
                S3Props.instance.getFileBucket(),
                S3Props.instance.getFileAccessKey(),
                S3Props.instance.getFileSecretKey()
        );
    }

    public CloudFileServiceS3OfSdkImp(Properties pops) {
        this(
                pops.getProperty("regionId"),
                pops.getProperty("bucket"),
                pops.getProperty("accessKey"),
                pops.getProperty("secretKey")
        );
    }

    public CloudFileServiceS3OfSdkImp(String regionId, String bucket, String accessKey, String secretKey) {
        this.regionId = regionId;

        this.bucketDef = bucket;

        this.accessKey = accessKey;
        this.secretKey = secretKey;

        //初始化client
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        client = AmazonS3ClientBuilder.standard()
                .withRegion(regionId)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        //初始化权限控制
        acls.grantPermission(GroupGrantee.AllUsers, Permission.Read);
    }

    @Override
    public Media get(String bucket, String key) throws CloudFileException {
        if (Utils.isEmpty(bucket)) {
            bucket = bucketDef;
        }

        try {
            GetObjectRequest request = new GetObjectRequest(bucket, key);

            S3Object obj = client.getObject(request);

            return new Media(obj.getObjectContent(), obj.getObjectMetadata().getContentType());
        } catch (Exception ex) {
            throw new CloudFileException(ex);
        }
    }

    @Override
    public Result put(String bucket, String key, Media media) throws CloudFileException {
        if (Utils.isEmpty(bucket)) {
            bucket = bucketDef;
        }

        String streamMime = media.contentType();
        if (Utils.isEmpty(streamMime)) {
            streamMime = "text/plain; charset=utf-8";
        }

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(streamMime);

            PutObjectRequest request = new PutObjectRequest(bucket, key, media.body(), metadata)
                    .withAccessControlList(acls);

            PutObjectResult tmp = client.putObject(request);

            return Result.succeed(tmp);
        } catch (Exception ex) {
            throw new CloudFileException(ex);
        }
    }

    @Override
    public Result delete(String bucket, String key) throws CloudFileException {
        if (Utils.isEmpty(bucket)) {
            bucket = bucketDef;
        }

        client.deleteObject(bucket, key);

        return Result.succeed();
    }
}