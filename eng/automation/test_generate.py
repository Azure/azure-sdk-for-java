import os
import tempfile
import unittest

from generate import update_revapi_skip

POM_WITH_REVAPI_TRUE = """\
<project>
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <jacoco.min.linecoverage>0</jacoco.min.linecoverage>
    <revapi.skip>true</revapi.skip>
  </properties>
</project>
"""

POM_WITH_REVAPI_FALSE = """\
<project>
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <jacoco.min.linecoverage>0</jacoco.min.linecoverage>
    <revapi.skip>false</revapi.skip>
  </properties>
</project>
"""

POM_WITHOUT_REVAPI = """\
<project>
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <jacoco.min.linecoverage>0</jacoco.min.linecoverage>
  </properties>
</project>
"""


class TestUpdateRevapiSkip(unittest.TestCase):

    def _write_and_update(self, content: str, beta: bool) -> str:
        fd, path = tempfile.mkstemp(suffix=".xml")
        try:
            with os.fdopen(fd, "w") as f:
                f.write(content)
            update_revapi_skip(path, beta)
            with open(path, "r") as f:
                return f.read()
        finally:
            os.unlink(path)

    # --- beta=True cases ---

    def test_beta_already_true_no_change(self):
        result = self._write_and_update(POM_WITH_REVAPI_TRUE, beta=True)
        self.assertEqual(result, POM_WITH_REVAPI_TRUE)

    def test_beta_false_flipped_to_true(self):
        result = self._write_and_update(POM_WITH_REVAPI_FALSE, beta=True)
        self.assertIn("<revapi.skip>true</revapi.skip>", result)
        self.assertNotIn("<revapi.skip>false</revapi.skip>", result)

    def test_beta_missing_added_true(self):
        result = self._write_and_update(POM_WITHOUT_REVAPI, beta=True)
        self.assertIn("<revapi.skip>true</revapi.skip>", result)
        self.assertIn("</properties>", result)

    # --- beta=False (stable) cases ---

    def test_stable_true_flipped_to_false(self):
        result = self._write_and_update(POM_WITH_REVAPI_TRUE, beta=False)
        self.assertIn("<revapi.skip>false</revapi.skip>", result)
        self.assertNotIn("<revapi.skip>true</revapi.skip>", result)

    def test_stable_already_false_no_change(self):
        result = self._write_and_update(POM_WITH_REVAPI_FALSE, beta=False)
        self.assertEqual(result, POM_WITH_REVAPI_FALSE)

    def test_stable_missing_not_added(self):
        result = self._write_and_update(POM_WITHOUT_REVAPI, beta=False)
        self.assertNotIn("revapi.skip", result)
        self.assertEqual(result, POM_WITHOUT_REVAPI)


if __name__ == "__main__":
    unittest.main()
