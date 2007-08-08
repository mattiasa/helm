#$LOAD_PATH << File.expand_path('../../../target/classes')
require 'java'

module HelmServerSpec
  include_class 'se.su.it.helm.HelmServer'
  
  describe HelmServer, "when first created" do
    before(:each) do
      A_PORT = 3000
      @helm = HelmServer.new(A_PORT)
    end
    it "should return a version string" do
      EXPECTED = 'helm-0.0.1'
      @helm.version.should eql(EXPECTED)
    end
    after do
      @helm = nil
    end
  end
end

