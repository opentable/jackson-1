VERSION=$1
CU=`pwd`
cd $CU
cp ../build/jackson-core-asl-$VERSION.jar .
cp ../build/jackson-mapper-asl-$VERSION.jar .

cd jackson-core-asl
mvn versions:set -DgroupId=org.codehaus.jackson -DnewVersion=$VERSION
cd $CU
cd jackson-mapper-asl
mvn versions:set -DgroupId=org.codehaus.jackson -DnewVersion=$VERSION -DprocessDependencies=true -DupdateMatchingVersions=true


mvn deploy:deploy-file -DgeneratePOM=false -DpomFile=$CU/jackson-core-asl/pom.xml -DrepositoryId=opentable -Dfile=$CU/jackson-core-asl-$VERSION.jar -Durl=http://artifactory.otenv.com/artifactory/internal-local

mvn deploy:deploy-file -DgeneratePOM=false \
  -DpomFile=$CU/jackson-mapper-asl/pom.xml \
  -Dfile=$CU/jackson-mapper-asl-$VERSION.jar \
  -DrepositoryId=opentable \
  -Durl=http://artifactory.otenv.com/artifactory/internal-local






