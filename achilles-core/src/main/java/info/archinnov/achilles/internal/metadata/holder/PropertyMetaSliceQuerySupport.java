package info.archinnov.achilles.internal.metadata.holder;

import com.google.common.collect.Iterables;
import info.archinnov.achilles.internal.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static info.archinnov.achilles.schemabuilder.Create.Options.ClusteringOrder;

public class PropertyMetaSliceQuerySupport extends PropertyMetaView {

    private static final Logger log = LoggerFactory.getLogger(PropertyMetaSliceQuerySupport.class);

    protected PropertyMetaSliceQuerySupport(PropertyMeta meta) {
        super(meta);
    }

    List<String> getPartitionKeysName(int size) {
        Validator.validateNotNull(meta.getEmbeddedIdProperties(), "Cannot get {} partition key names for entity '%s' because it does not have a compound primary key", size, meta.getEntityClassName());
        return meta.getEmbeddedIdProperties().getPartitionComponents().getCQL3ComponentNames().subList(0, size);
    }

    String getLastPartitionKeyName() {
        Validator.validateNotNull(meta.getEmbeddedIdProperties(), "Cannot get last partition key name for entity '%s' because it does not have a compound primary key", meta.getEntityClassName());
        return Iterables.getLast(meta.getEmbeddedIdProperties().getPartitionComponents().getCQL3ComponentNames());
    }

    List<String> getClusteringKeysName(int size) {
        Validator.validateNotNull(meta.getEmbeddedIdProperties(), "Cannot get {} clustering key names for entity '%s' because it does not have a compound primary key",size, meta.getEntityClassName());
        return meta.getEmbeddedIdProperties().getClusteringComponents().getCQL3ComponentNames().subList(0, size);
    }

    String getLastClusteringKeyName() {
        Validator.validateNotNull(meta.getEmbeddedIdProperties(), "Cannot get last clustering key name for entity '%s' because it does not have a compound primary key",meta.getEntityClassName());
        return Iterables.getLast(meta.getEmbeddedIdProperties().getClusteringComponents().getCQL3ComponentNames());
    }

    int getPartitionKeysSize() {
        return meta.getEmbeddedIdProperties().getPartitionComponents().propertyMetas.size();
    }

    int getClusteringKeysSize() {
        return meta.getEmbeddedIdProperties().getClusteringComponents().propertyMetas.size();
    }

    void validatePartitionComponents(Object...partitionComponents) {
        log.trace("Validate partition key components {} for entity class {}", partitionComponents, meta.getEntityClassName());
        Validator.validateNotNull(meta.getEmbeddedIdProperties(), "Cannot validate partition components for entity '%s' because it does not have a compound primary key",meta.getEntityClassName());
        meta.getEmbeddedIdProperties().getPartitionComponents().validatePartitionComponents(meta.getEntityClassName(), partitionComponents);
    }

    void validatePartitionComponentsIn(Object...partitionComponentsIN) {
        log.trace("Validate partition key components IN {} for entity class {}", partitionComponentsIN, meta.getEntityClassName());
        Validator.validateNotNull(meta.getEmbeddedIdProperties(), "Cannot validate partition components IN for entity '%s' because it does not have a compound primary key",meta.getEntityClassName());
        meta.getEmbeddedIdProperties().getPartitionComponents().validatePartitionComponentsIn(meta.getEntityClassName(), partitionComponentsIN);
    }

    void validateClusteringComponents(Object...clusteringKeys) {
        log.trace("Validate clustering keys {} for entity class {}", clusteringKeys, meta.getEntityClassName());
        Validator.validateNotNull(meta.getEmbeddedIdProperties(), "Cannot validate clustering keys for entity '%s' because it does not have a compound primary key",meta.getEntityClassName());
        meta.getEmbeddedIdProperties().getClusteringComponents().validateClusteringComponents(meta.getEntityClassName(), clusteringKeys);
    }

    void validateClusteringComponentsIn(Object...clusteringKeysIN) {
        log.trace("Validate clustering keys IN {} for entity class {}", clusteringKeysIN, meta.getEntityClassName());
        Validator.validateNotNull(meta.getEmbeddedIdProperties(), "Cannot validate clustering keys IN for entity '%s' because it does not have a compound primary key",meta.getEntityClassName());
        meta.getEmbeddedIdProperties().getClusteringComponents().validateClusteringComponentsIn(meta.getEntityClassName(), clusteringKeysIN);
    }

    public ClusteringOrder getClusteringOrder() {
        Validator.validateTrue(meta.structure().isClustered(),"Cannot get clustering order for entity {} because it is not clustered", meta.getEntityClassName());
        return meta.getEmbeddedIdProperties().getClusteringComponents().getClusteringOrders().get(0);
    }
}
