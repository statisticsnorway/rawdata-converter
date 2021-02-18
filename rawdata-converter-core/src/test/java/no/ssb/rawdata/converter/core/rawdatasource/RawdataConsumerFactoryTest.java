package no.ssb.rawdata.converter.core.rawdatasource;

import de.huxhorn.sulky.ulid.ULID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RawdataConsumerFactoryTest {

    @Test
    void whenIntitialPositionIsLast_thenWeShouldNotIncludeThatPosition() {
        RawdataConsumerFactory factory = new RawdataConsumerFactory(null, null);
        assertThat(factory.includeInitialPosition("LAST")).isFalse();
        assertThat(factory.includeInitialPosition("last")).isFalse();
        assertThat(factory.includeInitialPosition("lAst")).isFalse();
    }

    @Test
    void whenIntitialPositionIsFirst_thenWeShouldIncludeThatPosition() {
        RawdataConsumerFactory factory = new RawdataConsumerFactory(null, null);
        assertThat(factory.includeInitialPosition("FIRST")).isTrue();
        assertThat(factory.includeInitialPosition("first")).isTrue();
        assertThat(factory.includeInitialPosition("FirsT")).isTrue();
        assertThat(factory.includeInitialPosition(new ULID().nextULID())).isTrue();
    }

    @Test
    void whenIntitialPositionIsASpecificPositoin_thenWeShouldIncludeThatPosition() {
        RawdataConsumerFactory factory = new RawdataConsumerFactory(null, null);
        assertThat(factory.includeInitialPosition(new ULID().nextULID())).isTrue();
    }

    @Test
    void whenIntitialPositionIsNotSpecifiedOrGibberish_thenWeShouldStartFromTheInclusiveDeterminedPosition() {
        RawdataConsumerFactory factory = new RawdataConsumerFactory(null, null);
        assertThat(factory.includeInitialPosition(new ULID().nextULID())).isTrue();
        assertThat(factory.includeInitialPosition("gibberish")).isTrue();
        assertThat(factory.includeInitialPosition(null)).isTrue();
        assertThat(factory.includeInitialPosition("")).isTrue();
    }

}