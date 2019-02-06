#!/bin/sh

DIRECTORY="$HOME/bin"
if [ ! -d "$DIRECTORY" ]; then
  mkdir $DIRECTORY
fi

mvn clean package -f consumer-pom.xml -Dmaven.test.skip=true

APP_HOME=`pwd`
cp $APP_HOME/src/script/html2pdf.sh $DIRECTORY
cp $APP_HOME/target/html2pdf.jar $DIRECTORY
chmod 755 $DIRECTORY/html2pdf.sh

sed -e "s|APPHOME|$DIRECTORY|g" -i -- $DIRECTORY/html2pdf.sh
rm $DIRECTORY/html2pdf.sh--

rm /usr/local/bin/html2pdf
ln -s $DIRECTORY/html2pdf.sh /usr/local/bin/html2pdf

echo "html2pdf consumer installed!"