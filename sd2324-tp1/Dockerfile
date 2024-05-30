FROM nunopreguica/sd2324testerbase

# working directory inside docker image
WORKDIR /home/sd

ADD hibernate.cfg.xml .
ADD shorts.props .

# COPY tls/*.jks /home/sd/

COPY tls/*.jks /home/sd/

# copy the jar created by assembly to the docker image
COPY target/*jar-with-dependencies.jar sd2324.jar

# source .bash_profile

# cd IdeaProjects/sd2324-proj/sd2324-tp1

# docker pull smduarte/sd2324-kafka

# mvn clean compile assembly:single docker:build

# docker build -t sd2324-tp2-xxxxx-yyyyy:latest .

############################################################# TESTER ################################################################################
# sh test-sd-tp2.sh -image sd2324-tp2-xxxxx-yyyyy
# sh test-sd-tp2.sh -image sd2324-tp2-xxxxx-yyyyy -test num -log ALL

# Private Key generation
# PASSWORD - SD1234

# USERS
# keytool -ext SAN=dns:users0-ourorg -genkey -alias users0-ourorg -keyalg RSA -validity 365 -keystore users0-ourorg.jks -storetype pkcs12

# SHORTS
# keytool -ext SAN=dns:shorts0-ourorg -genkey -alias shorts0-ourorg -keyalg RSA -validity 365 -keystore shorts0-ourorg.jks -storetype pkcs12
# keytool -ext SAN=dns:shorts1-ourorg -genkey -alias shorts1-ourorg -keyalg RSA -validity 365 -keystore shorts1-ourorg.jks -storetype pkcs12
# keytool -ext SAN=dns:shorts2-ourorg -genkey -alias shorts2-ourorg -keyalg RSA -validity 365 -keystore shorts2-ourorg.jks -storetype pkcs12

# BLOBS
# keytool -ext SAN=dns:blobs0-ourorg -genkey -alias blobs0-ourorg -keyalg RSA -validity 365 -keystore blobs0-ourorg.jks -storetype pkcs12
# keytool -ext SAN=dns:blobs1-ourorg -genkey -alias blobs1-ourorg -keyalg RSA -validity 365 -keystore blobs1-ourorg.jks -storetype pkcs12
# keytool -ext SAN=dns:blobs2-ourorg -genkey -alias blobs2-ourorg -keyalg RSA -validity 365 -keystore blobs2-ourorg.jks -storetype pkcs12
# keytool -ext SAN=dns:blobs3-ourorg -genkey -alias blobs3-ourorg -keyalg RSA -validity 365 -keystore blobs3-ourorg.jks -storetype pkcs12

# Self-signed Server Certificate

# USERS
# keytool -exportcert -alias users0-ourorg -keystore users0-ourorg.jks -file users0-ourorg.cert

# SHORTS
# keytool -exportcert -alias shorts0-ourorg -keystore shorts0-ourorg.jks -file shorts0-ourorg.cert
# keytool -exportcert -alias shorts1-ourorg -keystore shorts1-ourorg.jks -file shorts1-ourorg.cert
# keytool -exportcert -alias shorts2-ourorg -keystore shorts2-ourorg.jks -file shorts2-ourorg.cert

# BLOBS
# keytool -exportcert -alias blobs0-ourorg -keystore blobs0-ourorg.jks -file blobs0-ourorg.cert
# keytool -exportcert -alias blobs1-ourorg -keystore blobs1-ourorg.jks -file blobs1-ourorg.cert
# keytool -exportcert -alias blobs2-ourorg -keystore blobs2-ourorg.jks -file blobs2-ourorg.cert
# keytool -exportcert -alias blobs3-ourorg -keystore blobs3-ourorg.jks -file blobs3-ourorg.cert

# Truststore

# USERS
# keytool -importcert -file users0-ourorg.cert -alias users0-ourorg -keystore client-ts.jks

# SHORTS
# keytool -importcert -file shorts0-ourorg.cert -alias shorts0-ourorg -keystore client-ts.jks
# keytool -importcert -file shorts1-ourorg.cert -alias shorts1-ourorg -keystore client-ts.jks
# keytool -importcert -file shorts2-ourorg.cert -alias shorts2-ourorg -keystore client-ts.jks

#BLOBS
# keytool -importcert -file blobs0-ourorg.cert -alias blobs0-ourorg -keystore client-ts.jks
# keytool -importcert -file blobs1-ourorg.cert -alias blobs1-ourorg -keystore client-ts.jks
# keytool -importcert -file blobs2-ourorg.cert -alias blobs2-ourorg -keystore client-ts.jks
# keytool -importcert -file blobs3-ourorg.cert -alias blobs3-ourorg -keystore client-ts.jks

############################################################# DROPBOX ################################################################################

# APP KEY
# cwendmo115gd0t1

# APP SECRET
# 3m4ddrj3nci3uv9

# ACCESS TOKEN
# sl.B2HcR_fzZyzo4AzPv-5WUWKgj0F84IGaebQvFchUhAGgBdsaEPmUd4qUi1mpZP813RApodvnwwZRJcgG2ynd7GIqfcLniz12eqb32oDdm0JNS_Xqy8Ca0AZOYwKDnUgR6Q2M-AkMqQqn