from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from tests.users import upgrade_users
from views.sign_in_view import SignInView
from time import sleep
from tests import app_path, common_password

#@marks.skip
class TestUpgradeApplication(SingleDeviceTestCase):

    @marks.testrail_id(6284)
    def test_apk_upgrade(self):
        import views.upgrade_dbs.chats.data as chat_data
        sign_in = SignInView(self.driver)
        chats = chat_data.chats
        home = sign_in.import_db(user=upgrade_users['chats'], import_db_folder_name='chats')

        home.just_fyi("Check chat previews")
        for chat in chats.keys():
            actual_chat_preview = home.get_chat(chat).chat_preview
            expected_chat_preview = chats[chat]['preview']
            if actual_chat_preview != expected_chat_preview:
                self.errors.append('Expected preview for %s is "%s", in fact "%s"' % (chat, expected_chat_preview, actual_chat_preview))

        home.just_fyi("Check unread indicator")
        if home.home_button.counter.text != '1':
            self.errors.append('New messages counter is not shown on Home button')
        unread_one_to_one_name, unread_public_name = 'All Whopping Dassierat', '#before-upgrade'
        unread_one_to_one, unread_public = home.get_chat(unread_one_to_one_name), home.get_chat(unread_public_name)
        if unread_one_to_one.new_messages_counter.text != chats[unread_one_to_one_name]['unread']:
            self.errors.append('New messages counter is not shown on chat element')
        if not unread_public.new_messages_public_chat.is_element_displayed():
            self.errors.append('Unread messages badge is not shown in public chat')

        home.just_fyi("Check pictures / add to contacts")
        not_contact = unread_one_to_one_name
        not_contact_chat = home.get_chat(not_contact).click()
        if not not_contact_chat.add_to_contacts.is_element_displayed():
            self.errors.append('Add to contacts is not shown in 1-1 chat')
        images = not_contact_chat.image_chat_item.find_elements()
        if len(images) != 2:
            self.errors.append('%s images are shown isntead of 2' % str(len(images)))
        for message in chats[not_contact]['messages']:
            if not not_contact_chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append('"%s" is not shown after upgrade' % message)
        home.home_button.double_click()
        if unread_one_to_one.new_messages_counter.text == '1':
            self.errors.append('New messages counter is shown on chat element after opening chat')


        # import pprint
        # pprint.pprint(len(images))



        # profile = home.profile_button.click()
        # profile.about_button.click()
        # old_version = profile.app_version_text.text


        # profile.upgrade_app()
        #
        # self.app = sign_in.driver.launch_app()
        # home = sign_in.sign_in()
        #
        # home.profile_button.click()
        # profile.about_button.click()
        # new_version = profile.app_version_text.text
        # print('Upgraded app version is %s vs base version is %s ' % (new_version, old_version))
        # assert new_version != old_version
        self.errors.verify_no_errors()


