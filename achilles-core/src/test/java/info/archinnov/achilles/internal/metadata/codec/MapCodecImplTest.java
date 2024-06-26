package info.archinnov.achilles.internal.metadata.codec;

import static org.fest.assertions.api.Assertions.*;

import com.google.common.collect.ImmutableMap;
import info.archinnov.achilles.exception.AchillesTranscodingException;
import info.archinnov.achilles.internal.metadata.codec.MapCodec;
import info.archinnov.achilles.internal.metadata.codec.MapCodecBuilder;
import info.archinnov.achilles.internal.metadata.codec.SimpleCodec;
import info.archinnov.achilles.internal.metadata.holder.PropertyType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class MapCodecImplTest {

    @Test
    public void should_encode_and_decode_a_complete_transformed_map() throws Exception {
        //Given
        SimpleCodec<PropertyType, String> keyCodec = new SimpleCodec<PropertyType, String>() {
            @Override
            public Class<PropertyType> sourceType() {
                return PropertyType.class;
            }

            @Override
            public Class<String> targetType() {
                return String.class;
            }

            @Override
            public String encode(PropertyType fromJava) throws AchillesTranscodingException {
                return fromJava.name();
            }

            @Override
            public PropertyType decode(String fromCassandra) throws AchillesTranscodingException {
                return PropertyType.valueOf(fromCassandra);
            }
        };

        SimpleCodec<Integer, String> valueCodec = new SimpleCodec<Integer, String>() {
            @Override
            public Class<Integer> sourceType() {
                return Integer.class;
            }

            @Override
            public Class<String> targetType() {
                return String.class;
            }

            @Override
            public String encode(Integer fromJava) throws AchillesTranscodingException {
                return fromJava.toString();
            }

            @Override
            public Integer decode(String fromCassandra) throws AchillesTranscodingException {
                return Integer.parseInt(fromCassandra);
            }
        };

        MapCodec<PropertyType, Integer, String, String> mapCodec = MapCodecBuilder
                .fromKeyType(PropertyType.class)
                .toKeyType(String.class)
                .withKeyCodec(keyCodec)
                .fromValueType(Integer.class)
                .toValueType(String.class)
                .withValueCodec(valueCodec);

        //When
        Map<String, String> encoded = mapCodec.encode(ImmutableMap.of(PropertyType.COUNTER, 1, PropertyType.ID, 2));
        Map<PropertyType, Integer> decoded = mapCodec.decode(ImmutableMap.of("LIST", "3", "SET", "4"));

        //Then
        assertThat(encoded.get("COUNTER")).isEqualTo("1");
        assertThat(encoded.get("ID")).isEqualTo("2");

        assertThat(decoded.get(PropertyType.LIST)).isEqualTo(3);
        assertThat(decoded.get(PropertyType.SET)).isEqualTo(4);
    }

    @Test
    public void should_encode_and_decoded_transformed_key_map() throws Exception {
        //Given
        SimpleCodec<PropertyType, String> keyCodec = new SimpleCodec<PropertyType, String>() {
            @Override
            public Class<PropertyType> sourceType() {
                return PropertyType.class;
            }

            @Override
            public Class<String> targetType() {
                return String.class;
            }

            @Override
            public String encode(PropertyType fromJava) throws AchillesTranscodingException {
                return fromJava.name();
            }

            @Override
            public PropertyType decode(String fromCassandra) throws AchillesTranscodingException {
                return PropertyType.valueOf(fromCassandra);
            }
        };

        MapCodec<PropertyType, String, String, String> mapCodec = MapCodecBuilder
                .fromKeyType(PropertyType.class)
                .toKeyType(String.class)
                .withKeyCodec(keyCodec)
                .withValueType(String.class);

        //When
        Map<String, String> encoded = mapCodec.encode(ImmutableMap.of(PropertyType.COUNTER, "1", PropertyType.ID, "2"));
        Map<PropertyType, String> decoded = mapCodec.decode(ImmutableMap.of("LIST", "3", "SET", "4"));

        //Then
        assertThat(encoded.get("COUNTER")).isEqualTo("1");
        assertThat(encoded.get("ID")).isEqualTo("2");

        assertThat(decoded.get(PropertyType.LIST)).isEqualTo("3");
        assertThat(decoded.get(PropertyType.SET)).isEqualTo("4");
    }

    @Test
    public void should_encode_and_decode_transformed_value() throws Exception {
        //Given
        SimpleCodec<Integer, String> valueCodec = new SimpleCodec<Integer, String>() {
            @Override
            public Class<Integer> sourceType() {
                return Integer.class;
            }

            @Override
            public Class<String> targetType() {
                return String.class;
            }

            @Override
            public String encode(Integer fromJava) throws AchillesTranscodingException {
                return fromJava.toString();
            }

            @Override
            public Integer decode(String fromCassandra) throws AchillesTranscodingException {
                return Integer.parseInt(fromCassandra);
            }
        };

        MapCodec<PropertyType, Integer, PropertyType, String> mapCodec = MapCodecBuilder
                .withKeyType(PropertyType.class)
                .fromValueType(Integer.class)
                .toValueType(String.class)
                .withValueCodec(valueCodec);

        //When
        Map<PropertyType, String> encoded = mapCodec.encode(ImmutableMap.of(PropertyType.COUNTER, 1, PropertyType.ID, 2));
        Map<PropertyType, Integer> decoded = mapCodec.decode(ImmutableMap.of(PropertyType.LIST, "3", PropertyType.SET, "4"));

        //Then
        assertThat(encoded.get(PropertyType.COUNTER)).isEqualTo("1");
        assertThat(encoded.get(PropertyType.ID)).isEqualTo("2");

        assertThat(decoded.get(PropertyType.LIST)).isEqualTo(3);
        assertThat(decoded.get(PropertyType.SET)).isEqualTo(4);
    }

    @Test
    public void should_encode_and_decode_pass_through_map() throws Exception {
        //Given
        MapCodec<PropertyType, Integer, PropertyType, Integer> mapCodec = MapCodecBuilder.withKeyType(PropertyType.class).withValueType(Integer.class);

        //When
        Map<PropertyType, Integer> encoded = mapCodec.encode(ImmutableMap.of(PropertyType.COUNTER, 1, PropertyType.ID, 2));
        Map<PropertyType, Integer> decoded = mapCodec.decode(ImmutableMap.of(PropertyType.LIST, 3, PropertyType.SET, 4));

        //Then
        assertThat(encoded.get(PropertyType.COUNTER)).isEqualTo(1);
        assertThat(encoded.get(PropertyType.ID)).isEqualTo(2);

        assertThat(decoded.get(PropertyType.LIST)).isEqualTo(3);
        assertThat(decoded.get(PropertyType.SET)).isEqualTo(4);
    }
}