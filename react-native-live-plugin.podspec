require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-live-plugin"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  react-native-live-plugin
                   DESC
  s.homepage     = "https://github.com/github_account/react-native-live-plugin"
  # brief license entry:
  s.license      = "MIT"
  # optional - use expanded license entry instead:
  # s.license    = { :type => "MIT", :file => "LICENSE" }
  s.authors      = { "Your Name" => "yourname@email.com" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/github_account/react-native-live-plugin.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,c,m,swift}"
  s.requires_arc = true

  s.resource_bundles = {
    "LivePluginImg" => ["ios/Resources/LivePlugin/Assets/*"]
  }

s.ios.vendored_library = "ios/Vender/CloudOpenSDK/libCloudOpenSDK.a","ios/Vender/EZOpenSDK/libEZOpenSDK.a"
  s.frameworks   =  "CoreMedia", "AudioToolbox","VideoToolbox","GLKit","OpenAL","MobileCoreServices","SystemConfiguration","CoreTelephony","AVFoundation"
  s.libraries    = "sqlite3.0","c++","iconv.2.4.0","bz2","z"

  s.dependency "React"
  # ...
  # s.dependency "..."
end

