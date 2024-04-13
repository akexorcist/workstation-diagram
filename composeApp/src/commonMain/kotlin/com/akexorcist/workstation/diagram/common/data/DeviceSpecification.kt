package com.akexorcist.workstation.diagram.common.data

data class DeviceSpecification(
    val type: Device.Type,
    val title: String,
    val subtitle: String?,
    val website: String?,
    val image: String,
    val description: String,
    val information: List<Pair<String, String>>,
)

fun Device.toDeviceSpecification(): DeviceSpecification =
    this.type.getAdditionalDeviceInformation()
        .let { (website, image, description, information) ->
            DeviceSpecification(
                type = this.type,
                title = this.title,
                subtitle = this.subtitle,
                website = website,
                image = image,
                description = description,
                information = information,
            )
        }

private data class AdditionalDeviceInformation(
    val website: String?,
    val image: String,
    val description: String,
    val information: List<Pair<String, String>>,
)

private fun Device.Type.getAdditionalDeviceInformation(): AdditionalDeviceInformation {
    return when (this) {
        Device.Type.OfficeLaptop -> AdditionalDeviceInformation(
            image = "image/office_laptop.webp",
            website = "https://support.apple.com/kb/SP854",
            description = "MacBook Pro 14\" 2021",
            information = listOf(
                "Chip" to "Apple M1 Pro\n" +
                        "10-core CPU with 8 performance cores and 2 efficiency cores\n" +
                        "14-core GPU\n" +
                        "16-core Neural Engine\n" +
                        "200GB/s memory bandwidth",
                "Display" to "14.2\" mini-LED backlit display\n" +
                        "Liquid Retina XDR display\n" +
                        "3,024 x 1,964 px\n" +
                        "XDR brightness: 1000 nits sustained full-screen, 1600 nits peak (HDR content only)\n" +
                        "SDR brightness: 500 nits max\n" +
                        "Wide color (P3)\n" +
                        "True Tone technology\n" +
                        "ProMotion technology for adaptive refresh rates up to 120Hz",
                "Memory" to "32GB",
                "Storage" to "512GB",
                "Wi-Fi" to "802.11ax Wi-Fi 6",
                "Bluetooth" to "Bluetooth 5.0",
                "Camera" to "1080p FaceTime HD camera",
                "Audio" to "High-fidelity six-speaker sound system with force-cancelling woofers\n" +
                        "Wide stereo sound\n" +
                        "Support for spatial audio when playing music or video with Dolby Atmos on built-in speakers\n" +
                        "Spatial audio with dynamic head tracking\n" +
                        "Studio-quality three-mic array with high signal-to-noise ratio and directional beamforming\n" +
                        "3.5 mm headphone jack with advanced support for high-impedance headphones",
                "Keyboard and Trackpad" to "Backlit Magic Keyboard\n" +
                        "Touch ID\n" +
                        "Ambient light sensor\n" +
                        "Force Touch trackpad for precise cursor control and pressure-sensing capabilities",
                "Ports" to "3 Thunderbolt 4 (USB-C) ports\n" +
                        "HDMI port\n" +
                        "SDXC card slot",
                "Power and Battery" to "70-watt-hour lithium-polymer battery",
                "Size" to "1.55 x 31.26 x 22.12 cm / 0.61 x 12.31 x8.71 in",
                "Weight" to "1.60 kg / 3.5 pounds",
            ),
        )

        Device.Type.PersonalLaptop -> AdditionalDeviceInformation(
            image = "image/personal_laptop.webp",
            website = "https://support.apple.com/kb/SP889",
            description = "MacBook Pro 14\" 2023",
            information = listOf(
                "Chip" to "Apple M2 Pro\n" +
                        "12-core CPU with 8 performance cores and 4 efficiency cores\n" +
                        "19-core GPU\n" +
                        "16-core Neural Engine\n" +
                        "200GB/s memory bandwidth",
                "Display" to "14.2\" mini-LED backlit display\n" +
                        "Liquid Retina XDR display\n" +
                        "3,024 x 1,964 px\n" +
                        "XDR brightness: 1000 nits sustained full-screen, 1600 nits peak (HDR content only)\n" +
                        "SDR brightness: 500 nits max\n" +
                        "Wide color (P3)\n" +
                        "True Tone technology\n" +
                        "ProMotion technology for adaptive refresh rates up to 120Hz",
                "Memory" to "32GB",
                "Storage" to "512GB",
                "Wi-Fi" to "802.11ax Wi-Fi 6E",
                "Bluetooth" to "Bluetooth 5.3",
                "Camera" to "1080p FaceTime HD camera",
                "Audio" to "High-fidelity six-speaker sound system with force-cancelling woofers\n" +
                        "Wide stereo sound\n" +
                        "Support for spatial audio when playing music or video with Dolby Atmos on built-in speakers\n" +
                        "Spatial audio with dynamic head tracking\n" +
                        "Studio-quality three-mic array with high signal-to-noise ratio and directional beamforming\n" +
                        "3.5 mm headphone jack with advanced support for high-impedance headphones\n" +
                        "HDMI port supports multichannel audio output",
                "Keyboard and Trackpad" to "Backlit Magic Keyboard\n" +
                        "Touch ID\n" +
                        "Ambient light sensor\n" +
                        "Force Touch trackpad for precise cursor control and pressure-sensing capabilities",
                "Ports" to "3 Thunderbolt 4 (USB-C) ports\n" +
                        "HDMI port\n" +
                        "SDXC card slot",
                "Power and Battery" to "70-watt-hour lithium-polymer battery",
                "Size" to "1.55 x 31.26 x 22.12 cm / 0.61 x 12.31 x8.71 in",
                "Weight" to "1.60 kg / 3.5 pounds",
            ),
        )

        Device.Type.PcDesktop -> AdditionalDeviceInformation(
            image = "image/pc_desktop.webp",
            website = null,
            description = "Custom Gaming PC",
            information = listOf(
                "OS" to "Windows 11",
                "Mainboard" to "GIGABYTE B560 HD3 (rev. 1.0)",
                "CPU" to "Intel Core i5-11400F Processor (12M Cache, up to 4.40 GHz)",
                "VGA" to "ASUS ROG Strix GTX 1080 Ti GAMING OC",
                "Memory" to "G.SKILL Trident Z RGB DDR4 3,200MHz 8GB x 2 ",
                "Storage" to "WD BLACK SN750 500GB SSD PCIe NVMe M.2 2280 x 2",
                "Case" to "Thermaltake Versa J21 Tempered Glass Edition",
                "Wireless Connectivity" to "ASUS AX3000 Dual band Gigabit Wi-Fi 6 Bluetooth 5.0",
                "Power Supply" to "Corsair Power Supply RM750 80 PLUS Gold",
            ),
        )

        Device.Type.NintendoSwitch -> AdditionalDeviceInformation(
            image = "image/pc_desktop.webp",
            website = null,
            description = "",
            information = listOf(),
        )

        Device.Type.PlayStation5 -> AdditionalDeviceInformation(
            image = "image/pc_desktop.webp",
            website = null,
            description = "",
            information = listOf(),
        )

        Device.Type.UsbDockingStation -> AdditionalDeviceInformation(
            image = "image/usb_docking_station.webp",
            website = "https://www.dell.com/support/manuals/en-us/dell-wd19tbs-dock/wd19tbs_user_guide/docking-specifications",
            description = "Docking Station with Thunderbolt",
            information = listOf(
                "Display Support with Thunderbolt input" to "For a HBR2* PC, 3 x FHD @ 60Hz, 3 x QHD @ 60Hz, 2 x 4K @ 30Hz\n" +
                        "For a HBR3 PC, 4 x FHD @ 60Hz, 4 x QHD @ 60Hz, 2 x 4K @ 60Hz",
                "Max Resolution" to "5K @ 60Hz with HBR2/HBR3, Thunderbolt systems\n" +
                        "8K @ 60Hz with HBR3,Thunderbolt systems supporting Display Stream Compression ",
                "Video interfaces" to "2 x Full size DP1.4\n" +
                        "1 x HDMI\n" +
                        "1 x MFDP USB-C\n" +
                        "1 x Thunderbolt 3 USB-C",
                "USB Ports" to "3 x USB-A 3.1 Gen 1\n" +
                        "2 x USB-C 3.1 Gen 2",
                "Docking Interface" to "Thunderbolt 3 (Type-C Connector)",
                "Docking Interface" to "Thunderbolt 3 (Type-C Connector)",
                "Cable Length" to "0.8 m",
                "Security Slot Type" to "1 x Kensington lock slot\n" +
                        "1 x Noble Wedge lock slot",
                "LED Indicators" to "Power Adapter LED\n" +
                        "Power Button LED\n" +
                        "RJ45 LEDs",
                "Power" to "Power Adapter 180 Watt AC with up to 130 Watt power delivery\n" +
                        "Up to 90 Watts power delivery to non-Dell systems",
                "Dimensions" to "205 x 90 x 29 mm | 8.07 x 3.54 x 1.14 in",
                "Dock Weight (without power adapter)" to "585 g | 1.29 lbs",
            ),
        )

        Device.Type.HdmiSwitcher -> AdditionalDeviceInformation(
            image = "image/pc_desktop.webp",
            website = null,
            description = "HDMI 2.1 Switch, 2 In 1 Out",
            information = listOf(),
        )

        Device.Type.DigitalCamera -> AdditionalDeviceInformation(
            image = "image/digital_camera.webp",
            website = "https://www.sony.co.th/th/electronics/interchangeable-lens-cameras/ilce-6000-body-kit",
            description = "E-mount camera with APS-C Sensor",
            information = listOf(
                "Lens Mount" to "Sony E-mount lenses",
                "Sensor" to "APS-C type (23.5 x 15.6mm)",
                "Number of pixels (Effective)" to "24.3MP",
                "Number of pixels (Total)" to "Approx. 24.7MP",
                "Image Sensor Aspect Ratio" to "3:02, 3:2",
                "Movie Recording Format" to "AVCHD 2.0 / MP4",
                "Focus Type" to "Fast Hybrid AF(phase-detection AF/contrast-detection AF)",
                "AF Illuminator Range" to "Approx. 0.3 - approx. 3.0m (with E PZ 16-50mm F3.5-5.6 OSS lens attached)",
                "Metering Sensor" to "Exmor APS HD CMOS sensor",
                "Metering Sensitivity" to "EV0 to EV20 (at ISO100 equivalent with F2.8 lens attached)",
                "Exposure Compensation" to "+/- 5.0EV(1/3 EV, 1/2 EV steps selectable)",
                "ISO Sensitivity" to "ISO 100-25600",
                "Viewfinder" to "0.39\"-type electronic viewfinder (color)\n" +
                        "1,440,000 dots",
                "LCD Screen" to "3.0\" wide type TFT LCD\n" +
                        "921,600 dots",
                "Shutter Speed" to "Still images:1/4000 to 30 sec, Bulb, Movies: 1/4000 to 1/4(1/3 steps) up to 1/60 in AUTO mode (up to 1/30 in Auto slow shutter mode)",
                "Flash Sync Speed" to "Still images:1/4000 to 30 sec, Bulb, Movies: 1/4000 to 1/4(1/3 steps) up to 1/60 in AUTO mode (up to 1/30 in Auto slow shutter mode)",
                "Flash Compensation" to "+/- 3.0 EV (switchable between 1/3 and 1/2 EV steps)",
                "Interfaces" to "4Kstill image PB\n" +
                        "BRAVIA Sync (link menu)\n" +
                        "HDMI micro connector (Type-D)\n" +
                        "Multi/Micro USB Terminal\n" +
                        "Multi Interface Shoe\n" +
                        "NFC\n" +
                        "PC Remote\n" +
                        "PhotoTV HD\n" +
                        "Wireless LAN (built-in)",
                "Supplied Batter" to "NP-FW50 W-series Rechargeable Battery Pack",
                "Internal Battery Charge" to "Yes",
                "Dimensions" to "120 x 66.9 x 45.1 mm",
                "Weight" to "285 g (Body Only) / 344 g (With battery and media)",
            ),
        )

        Device.Type.HdmiToWebcam -> AdditionalDeviceInformation(
            image = "image/hdmi_webcam.webp",
            website = "https://www.elgato.com/ww/en/p/cam-link-4k",
            description = "Turn your camera into a webcam",
            information = listOf(
                "Input" to "HDMI (unencrypted)",
                "Supported Resolutions" to "3840x2160 up to p30\n" +
                        "1920x1080 up to p60 / i60\n" +
                        "1280x720 up to p60\n" +
                        "720x576p50\n" +
                        "720x480p60",
                "Dimensions" to "8.1 x 1.2 x 3.1 cm / 3.18 x 0.47 x 1.22 in",
                "Weight" to "20 g / 0.044 lb",
            ),
        )

        Device.Type.StreamDeck -> AdditionalDeviceInformation(
            image = "image/stream_deck.webp",
            website = "https://www.elgato.com/ww/en/p/stream-deck-mk2-black",
            description = "15 customizable LCD keys to control apps and platforms",
            information = listOf(
                "Keys" to "15 customizable LCD keys",
                "Interface" to "USB 2.0",
                "Dimensions" to "118 x 84 x 25 mm / 4.6 x 3.3 x 1.0 in. (without stand)",
                "Weight" to "145 g without stand / 270 g with stand",
            ),
        )

        Device.Type.ExternalSsd -> AdditionalDeviceInformation(
            image = "image/external_ssd.webp",
            website = "https://www.seagate.com/as/en/support/external-hard-drives/portable-hard-drives/fast-ssd/",
            description = "Portable Storage High-performance USB-C external SSD",
            information = listOf(
                "Capacity" to "500GB",
                "Interface" to "USB 3.0",
                "Read/Write Speed" to "Up to 540/500 MB/s",
                "Dimensions" to "94 x 79 x 9.0 mm / 3.701 x 3.11 x 0.354 in",
                "Weight" to "82 g / 0.181 lb",
            ),
        )

        Device.Type.UsbCSwitcher -> AdditionalDeviceInformation(
            image = "image/usb_c_switcher.webp",
            website = "https://www.aten.com/global/en/products/usb-solutions/docks-and-switches/us3342/",
            description = "2-Port USB-C Gen 2 Sharing Switch with Power Pass-through",
            information = listOf(
                "Housing" to "Aluminum",
                "Computer Connections" to "2",
                "Computer Interface" to "USB 3.2 Gen 2 Type-C Female (Black) x 2",
                "Device Interface" to "USB 3.2 Gen 2 Type-A Female (Blue) × 3\n" +
                        "USB 3.2 Gen 2 Type-C Female (black) x 1",
                "Power Interface" to "USB Type-C Female x 1, supports USB PD3.0 at 5V, 9V, 15V and 20V output",
                "Port Selection" to "Remote port selector / software",
                "Power Consumption" to "DC5V:0.43W:428BTU",
                "LED" to "2 (White)",
                "Dimensions" to "14.30 x 9.00 x 2.38 cm | 5.63 x 3.54 x 0.94 in",
                "Weight" to "0.26 kg | 0.57 lb",
            ),
        )

        Device.Type.UsbHub -> AdditionalDeviceInformation(
            image = "image/usb_hub.webp",
            website = "https://www.orico.cc/usmobile/product/detail/id/7415",
            description = "Mini 3-in-1 USB Hub",
            information = listOf(
                "Material" to "ABS",
                "Input Interface" to "USB 3.0",
                "Output Interface" to "USB 3.0 x 1, USB 2.0 x 2",
                "Transmission Rate" to "5Gbps (USB 3.0), 480Mbps (USB 2.0)",
                "Power" to "5V 0.5A",
                "Dimensions" to "55 x 25 x 20 mm",
            ),
        )

        Device.Type.UsbPowerAdapter -> AdditionalDeviceInformation(
            image = "image/usb_power_adapter.webp",
            website = "https://aukey.com.my/products/pa-t11-6-usb-port-qualcomm-quick-charge-3-0-desktop-charger",
            description = "6 USB Port Qualcomm Quick Charge 3.0 Desktop Charger",
            information = listOf(
                "Technology" to "Quick Charge 3.0, AiPower",
                "Input" to "100-240V",
                "Output (AiPower):" to "5V 2.4A per port",
                "Output (Quick Charge 3.0)" to "3.6V-6.5V 3A | 6.5V-9V 2A | 9V-12V 1.5A",
                "Dimensions" to "9.4 x 6 x 2.5 cm",
                "Weight" to "207 g",
            ),
        )

        Device.Type.SecondaryMonitor -> AdditionalDeviceInformation(
            image = "image/secondary_monitor.webp",
            website = "https://www.arzopa.com/products/a1-15-6-fhd-1080p-portable-monitor",
            description = "15.6\" FHD 1080P Portable Monitor",
            information = listOf(
                "Screen Size" to "15.6\"",
                "Panel Type" to "IPS",
                "Aspect Ratio" to "16:9",
                "Resolution" to "1,920 x 1,080 px",
                "Refresh Rate" to "60Hz",
                "Display Color" to "262K 6-bit",
                "Color Temperature" to "6,800K",
                "Visual Angle" to "H：178° V：178°",
                "Contrast Ratio" to "800:1",
                "Brightness" to "250 cd/m²",
                "Color Gamut" to "45% NTCS",
                "Input Interface" to "Mini HD(Video Signal), Type-C Full Function(Video Data, Power Supply ISDN)",
                "Output Interface" to "3.5mm Headphone Interface",
                "Speaker" to "Built-in Speakers (1W x 2)",
                "Dimensions" to "24.3 x 35.3 x 0.9 cm",
                "Weight" to "1.44 lb",
            ),
        )

        Device.Type.PrimaryMonitor -> AdditionalDeviceInformation(
            image = "image/primary_monitor.webp",
            website = "https://www.dell.com/support/home/en-th/product-support/product/dell-u2722d-monitor/overview",
            description = "27\" QHD IPS Monitor",
            information = listOf(
                "Screen Size" to "27\"",
                "Active Display Area" to "596.7 x 335.7 mm | 23.49 x 13.21 in",
                "Maximum Resolution" to "2,560 x 1,440 @ 60Hz",
                "Aspect Ratio" to "16:9",
                "Pixel Pitch " to "0.2331 mm x 0.2331 mm",
                "Brightness" to "350 cd/m²  (typical)",
                "Color Gamut" to "100% sRGB, 100% Rec.709, 95% DCI-P3",
                "Color Depth" to "1.07 billion colors",
                "Contrast Ratio" to "1000: 1  (typical)",
                "Viewing Angle" to "178° vertical / 178° horizontal",
                "Response Time" to "8 ms (Normal mode); 5 ms (Fast mode)",
                "Panel Technology " to "IPS",
                "Backlight Technology" to "LED",
                "Display Screen Coating" to "Anti-glare treatment of the front polarizer (3H) hard coating",
                "Connectors" to "1 x DP 1.4 (HDCP 1.4)\n" +
                        "1 x HDMI 1.4\n" +
                        "1 x USB-C (USB 3.2 Gen2, 10Gbps) upstream port, data only\n" +
                        "1 x DP (Out)\n" +
                        "2 x super speed USB-A (USB 3.2 Gen2, 10Gbps) downstream ports\n" +
                        "1 x super speed USB-A (USB 3.2 Gen2, 10Gbps) with B.C 1.2\n" +
                        "1 x USB-C (USB 3.2 Gen2, 10Gbps, Up to 15W charging) downstream port\n" +
                        "1 x Analog 2.0 audio line out (3.5mm jack)",
                "VESA Mounting Support" to "VESA mounting holes (100 x 100 mm)",
                "AC Input" to "100 VAC to 240 VAC / 50 Hz or 60 Hz ± 3 Hz / 1.6 A (maximum)",
                "Power Consumption" to "<0.3 W (Off Mode)\n" +
                        "<0.5 W (Standby Mode)\n" +
                        "25 W (On Mode)\n" +
                        "74 W (Maximum)",
                "Dimension" to "385.1 - 535.1 x 611.3 x 185.0 mm | 15.16 - 21.07 x 24.07 x 7.28 in)",
                "Weight (panel only)" to "4.38 kg | (9.66 lb",
                "Weight (with stand and cables)" to "6.61 kg (14.57 lb)",
            ),
        )

        Device.Type.UsbDac -> AdditionalDeviceInformation(
            image = "image/usb_dac.webp",
            website = "https://steelseries.com/gaming-accessories/gamedac-gen-2",
            description = "Hi-Res Certified DAC for PC and PlayStation",
            information = listOf(
                "Connections" to "2x USB-C System Connections, 1x Line-in, 1x Line-out",
                "Digital-to-analog converter" to "ESS Sabre 9218PQ40",
                "Max Audio Format" to "96 kHz, 24 bit",
                "Frequency Response" to "5-40,000 Hz",
                "Signal-to-noise ratio (SNR)" to "111 dB"
            ),
        )

        Device.Type.UsbDongle1 -> AdditionalDeviceInformation(
            image = "image/usb_dongle_1.webp",
            website = "https://www.logitechg.com/en-us/products/gaming-mice/pro-x-superlight-wireless-mouse.910-005878.html",
            description = "USB wireless receiver for Wireless Gaming Mouse",
            information = listOf(
                "Microprocessor" to "32-bit ARM",
                "Sensor" to "HERO",
                "Resolution" to "100 – 25,600 dpi",
                "Max. acceleration" to ">1.41 oz (40 g)",
                "Max. speed" to "400 IPS",
                "USB report rate" to "1 ms (1000 Hz)",
                "Batter" to "Constant motion: 70h",
                "Dimension" to "125 x 63.5 x 40 mm | 4.92 x 2.50 x 1.57 in",
                "Weight" to "63 g | 2.22 oz",
            ),
        )

        Device.Type.UsbDongle2 -> AdditionalDeviceInformation(
            image = "image/usb_dongle_2.webp",
            website = "https://www.logitech.com/en-us/products/mice/logi-bolt-usb-receiver.956-000007.html",
            description = "USB wireless receiver for Logi Bolt",
            information = listOf(
                "Dimensions" to "6.11 x 14.4 x 18.65 mm | 0.24 x 0.57 x 0.73 in",
                "Weight" to "1.68 g | 0.06 oz",
            ),
        )

        Device.Type.LedLamp -> AdditionalDeviceInformation(
            image = "image/led_light_bar.webp",
            website = "https://www.baseus.com/products/i-wok-monitor-light-bar",
            description = "Monitor Light Bar for Computer",
            information = listOf(
                "Material" to "Aluminum",
                "Wattage" to "5W",
                "Voltage" to "5V",
                "Switch Style" to "Touch",
                "Dimensions" to "2.59 x 17.71 x 3.62 in",
                "Weight" to "1.61 pounds",
            ),
        )

        Device.Type.Speaker -> AdditionalDeviceInformation(
            image = "image/speaker.webp",
            website = "https://www.bose.com/p/speakers/bose-soundlink-mini-ii-special-edition/SLMINIISE-SPEAKERWIRELESS.html",
            description = "Bluetooth Speaker",
            information = listOf(
                "Material" to "Aluminum, Plastic, Steel, Metal, Silicone",
                "Microphones" to "Built-in Microphone",
                "Sound Options" to "Speakerphone",
                "Battery Life" to "12 hours",
                "Battery Charge Time" to "4 hours",
                "Charging Interface" to "USB C",
                "Wireless Connectivity" to "Bluetooth 3.0 up to 30 ft (9 m)",
                "Dimension" to "2 x 7.06 x 2.31 in",
                "Weight" to "1.44 lb",
            ),
        )

        Device.Type.Microphone1 -> AdditionalDeviceInformation(
            image = "image/microphone_1.webp",
            website = "https://nzxt.com/product/capsule",
            description = "Cardioid USB Gaming Microphone",
            information = listOf(
                "Element" to "Condenser",
                "Polar Pattern" to "Cardioid",
                "Frequency Response" to "20Hz - 20kHz",
                "Maximum Sound Pressure Level (SPL)" to "120dB",
                "Total Harmonic Distortion (THD)" to "0.1% @ 1kHz",
                "Bit Depth" to "24-bit",
                "Sample Rate" to "96kHz",
                "Output Connector" to "USB-C",
                "Materials" to "Extruded Aluminum Structure with PC ABS Shell",
                "Dimensions" to "60 x 65.1 x 170.2mm (without stand)",
                "Weight" to "314g (without stand)",
            ),
        )

        Device.Type.Microphone2 -> AdditionalDeviceInformation(
            image = "image/microphone_2.webp",
            website = "https://www.shure.com/en-US/products/microphones/mv7",
            description = "Professional-quality USB dynamic microphone",
            information = listOf(
                "Type" to "Dynamic (moving coil)",
                "Polar Pattern" to "Unidirectional (cardioid)",
                "A/D Converter" to "16 or 24-bit, 44.1 or 48 kHz",
                "Frequency Response" to "50 Hz to 16,000 Hz",
                "Adjustable Gain Range" to "0 to +36 dB",
                "XLR Sensitivity" to "-55 dBV/Pa (1.78 mV) at 1 kHz",
                "USB Sensitivity" to "-47 dBFS/Pa at 1 kHz",
                "USB Maximum SPL" to "132 dB SPL",
                "Headphone Output" to "3.5 mm (1/8\")",
                "XLR Output Impedance" to "314 Ω at 1 kHz",
                "Power Requirements" to "Powered through USB or Lightning connector",
                "MFi Certified" to "Yes",
                "Connector Type" to "Micro-B USB and XLR",
                "Mounting Type" to "5/8\"-27 thread mount",
                "Housing" to "All-metal construction",
                "Dimensions" to "Length 53.6 mm; Diameter 66.5 mm",
                "Weight" to "0.55 kg | 1.21 lbs",
            ),
        )

        Device.Type.HdmiCapture -> AdditionalDeviceInformation(
            image = "image/hdmi_capture.webp",
            website = "https://www.elgato.com/us/en/p/game-capture-hd60-x",
            description = "Video Capture Device",
            information = listOf(
                "Input" to "HDMI (unencrypted) ",
                "Output" to "HDMI (lag-free passthrough) up to 2160p60, 1440p120, 1080p240, VRR, HDR ",
                "Capture Resolutions" to "2160p30, 1440p60, 1080p60, 1080p30, 1080i, 720p60, 576p, 480p\n" +
                        "HDR 10-bit passthrough (up to 4K60) / capture (up to 1080p60)",
                "Dimensions" to "112 x 72 x 18 mm / 4.4 x 2.8 x 0.7 in",
                "Weight" to "91 g / 3.2 oz",
            ),
        )

        Device.Type.AndroidDevice -> AdditionalDeviceInformation(
            image = "image/android_device.webp",
            website = "https://www.gsmarena.com/oppo_a18-12591.php",
            description = "Test device for app development",
            information = listOf(
                "CPU" to "Mediatek MT6769 Helio G85",
                "GPU" to "Mali-G52 MC2",
                "Display" to "IPS LCD, 90Hz, 720 nits\n" +
                        "6.56\" (20:9) 720 x 1612 px",
                "OS" to "Android 13, upgradable to Android 14, ColorOS 14",
                "Memory" to "4GB",
                "Storage" to "128GB eMMC 5.1",
                "SD Card" to "microSDXC",
                "SIM" to "Nano-SIM, dual stand-by",
                "Back Camera" to "8 MP, f/2.0, (wide), AF\n" +
                        "2 MP, f/2.4, (depth)\n" +
                        "1080p@30fps video recording",
                "Rear Camera" to "5 MP, f/2.2, (wide)\n" +
                        "1080p@30fps video recording",
                "WLAN" to "Wi-Fi 802.11 a/b/g/n/ac, dual-band",
                "Bluetooth" to "Bluetooth 5.3, A2DP, LE, aptX HD",
                "Location" to "GPS, GALILEO, GLONASS, BDS, QZSS",
                "NFC" to "No",
                "USB" to "USB Type-C 2.0",
                "Battery" to "Li-Po 5000 mAh",
                "Dimensions" to "163.7 x 75 x 8.2 mm / 6.44 x 2.95 x 0.32 in",
                "Weight" to "188 g / 6.63 oz",
            ),
        )

        Device.Type.WirelessCharger -> AdditionalDeviceInformation(
            image = "image/wireless_charger.webp",
            website = "https://www.ikea.com/us/en/p/nordmaerke-wireless-charger-textile-gray-10491658/",
            description = "Charging pad",
            information = listOf(
                "Input" to "5.0V DC, 2.0A USB Type-C",
                "Output power" to " 36 dBµA/m at 3m",
                "Operating frequency" to "110.3KHz – 148 KHz",
                "Compatibility" to " Qi 1.2.4BPP",
            ),
        )

        Device.Type.Headphone -> AdditionalDeviceInformation(
            image = "image/gaming_headset.webp",
            website = "https://steelseries.com/gaming-headsets/arctis-nova-pro",
            description = "Wired High-Fidelity Gaming Audio",
            information = listOf(
                "Neodymium Drivers" to "40 mm",
                "Headphone Frequency Response" to "10–40,000 Hz",
                "Headphone Sensitivity" to "93 dBSPL",
                "Headphone Impedance" to "38 Ohm",
                "Headphone Total Harmonic Distortion" to "< 1%",
                "Microphone Type" to "ClearCast Gen 2 - Fully Retractable Boom",
                "Microphone Polar Pattern" to "Bidirectional Noise-Canceling",
                "Microphone Frequency Response" to "100–10,000 Hz",
                "Microphone Sensitivity" to "-38 dBV/Pa",
                "Microphone Impedance" to "2,200 Ohm",
                "Dimensions" to "7.58 x 7.37 x 3.44 in",
                "Weight" to "2.51 pounds",
            ),
        )
    }
}
