package info.archinnov.achilles.internal.metadata.holder;

import static org.fest.assertions.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class PropertyMetaCacheSupportTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertyMeta meta;

    @Test
    public void should_extract_clustered_fields_from_embedded_id() throws Exception {
        //Given
        when(meta.structure().isEmbeddedId()).thenReturn(true);
        when(meta.getEmbeddedIdProperties().getCQL3ComponentNames()).thenReturn(Arrays.asList("a", "b"));
        final PropertyMetaCacheSupport view = new PropertyMetaCacheSupport(meta);

        //When
        final Set<String> fields = view.extractClusteredFieldsIfNecessary();

        //Then
        assertThat(fields).containsOnly("a", "b");
    }

    @Test
    public void should_extract_clustered_fields_from_normal_id() throws Exception {
        //Given
        when(meta.structure().isEmbeddedId()).thenReturn(false);
        when(meta.getCQL3ColumnName()).thenReturn("a");
        final PropertyMetaCacheSupport view = new PropertyMetaCacheSupport(meta);

        //When
        final Set<String> fields = view.extractClusteredFieldsIfNecessary();

        //Then
        assertThat(fields).containsOnly("a");
    }


}