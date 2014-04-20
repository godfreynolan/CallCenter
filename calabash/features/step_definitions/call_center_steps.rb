
Given(/^I am on the "(.*?)" screen$/) do |text|
 sleep 5
 performAction('assert_text',text, true) 
end


When(/^I enter "(.*?)" into the username field$/) do |username|
  performAction('enter_text_into_id_field',username, 'username_field')
end

When(/^I enter "(.*?)" into the password field$/) do |password|
  performAction('enter_text_into_id_field', password, 'password_field')
end

When(/^I enter "(.*?)" into the server field$/) do |server|
   performAction('enter_text_into_id_field', server, 'url_field')
end

Then(/^I see "(.*?)" screen$/) do |text|
  sleep 5
  performAction('assert_text',text, true) 
end

Then(/^I see the status has changed$/) do 
  verifystatus = page(CallCenterPage).await  
  verifystatus.verify_change
end

Then(/^I change the status$/) do
  userstatus = page(CallCenterDetailsPage).await    
  userstatus.get_status(1)
 end


#Background navigation
Given(/^I have logged in successfully$/) do
  macro 'I see "Call Queue Manager"'  
  macro 'I enter text "gnolanUser1@xdp.broadsoft.com" into field with id "username_field"'
  macro 'I enter text "welcome1" into field with id "password_field"'
  macro 'I enter text "http://xsp2.xdp.broadsoft.com" into field with id "url_field"'
  macro 'I press view with id "login_button"'
  macro 'I see "Call Centers"'
  performAction('wait', 3)
end