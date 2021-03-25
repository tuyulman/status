from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from tests.users import upgrade_users
from tests import app_path, common_password

#@marks.skip
class TestUpgradeApplication(SingleDeviceTestCase):

    @marks.testrail_id(6284)
    def test_apk_upgrade(self):
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(upgrade_user['passphrase'])
        profile = home.profile_button.click()
        # profile.about_button.click()
        # old_version = profile.app_version_text.text
        profile.logout()
        self.driver.push_file(source_path='/Users/curikovatm/Downloads/export.db',
                              destination_path=app_path + 'export.db')
        sign_in.multi_account_on_login_button.wait_for_visibility_of_element(30)
        sign_in.get_multiaccount_by_position(1).click()
        sign_in.password_input.set_value(common_password)
        sign_in.options_button.click()
        sign_in.element_by_text('Import unencrypted').click()
        sign_in.element_by_text('Import unencrypted').wait_for_invisibility_of_element(40)
        sign_in.sign_in_button.click()
        sign_in.home_button.wait_for_element(40)
        import time
        sign_in.just_fyi("start sleep")
        time.sleep(30)

        #profile.upgrade_app()
        #
        # self.app = sign_in.driver.launch_app()
        # home = sign_in.sign_in()
        #
        # profile = home.profile_button.click()
        # profile.about_button.click()
        # new_version = profile.app_version_text.text
        # print('Upgraded app version is %s vs base version is %s ' % (new_version, old_version))
        # assert new_version != old_version


