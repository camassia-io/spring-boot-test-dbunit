package io.camassia.spring.dbunit.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class NestedClassUtilTest {

    @Nested
    inner class GetHierarchy {
        @Test
        fun shouldExtractZeroLevelHierarchy() {
            val hierarchy = NestedClassUtil.getHierarchy(NestedClassUtilTest::class.java)

            assertThat(hierarchy).hasSize(1).containsExactly(NestedClassUtilTest::class.java)
        }

        @Test
        fun shouldExtractOneLevelHierarchy() {
            val hierarchy = NestedClassUtil.getHierarchy(LevelOne::class.java)

            assertThat(hierarchy).hasSize(2).containsExactly(
                NestedClassUtilTest::class.java,
                LevelOne::class.java
            )
        }

        @Test
        fun shouldExtractTwoLevelHierarchy() {
            val hierarchy = NestedClassUtil.getHierarchy(LevelOne.LevelTwo::class.java)

            assertThat(hierarchy).hasSize(3).containsExactly(
                NestedClassUtilTest::class.java,
                LevelOne::class.java,
                LevelOne.LevelTwo::class.java
            )
        }

        @Test
        fun shouldExtractOneLevelHierarchy_withSpecialName() {
            val hierarchy = NestedClassUtil.getHierarchy(`Special $ name`::class.java)

            assertThat(hierarchy).hasSize(2).containsExactly(
                NestedClassUtilTest::class.java,
                `Special $ name`::class.java
            )
        }
    }



    @Nested
    inner class LevelOne {
        @Nested
        inner class LevelTwo
    }

    @Nested
    inner class `Special $ name`
}
