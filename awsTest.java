package aws;

/*
* Cloud Computing
* 
* Dynamic Resource Management Tool
* using AWS Java SDK Library
* 
*/
import java.util.Base64;
import java.util.Iterator;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import com.jcraft.jsch.*;
import java.io.*;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Tag;

public class awsTest {

	static AmazonEC2      ec2;

	private static void init() throws Exception {

		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
					"Please make sure that your credentials file is at the correct " +
					"location (~/.aws/credentials), and is in valid format.",
					e);
		}
		ec2 = AmazonEC2ClientBuilder.standard()
			.withCredentials(credentialsProvider)
			.withRegion("us-east-1")	/* check the region at AWS console */
			.build();
	}

	public static void main(String[] args) throws Exception {

		init();

		Scanner menu = new Scanner(System.in);
		Scanner id_string = new Scanner(System.in);
		int number = 0;
		
		while(true)
		{
			System.out.println("                                                            ");
			System.out.println("                                                            ");
			System.out.println("------------------------------------------------------------");
			System.out.println("           Amazon AWS Control Panel using SDK               ");
			System.out.println("------------------------------------------------------------");
			System.out.println("  1. list instance                2. available zones        ");
			System.out.println("  3. start instance               4. available regions      ");
			System.out.println("  5. stop instance                6. create instance        ");
			System.out.println("  7. reboot instance              8. list images            ");
			System.out.println("  9. condor_status (host)        10. create IAM user group  ");
			System.out.println("                    	     99. quit                   ");
			System.out.println("------------------------------------------------------------");
			
			System.out.print("Enter an integer: ");
			
			if(menu.hasNextInt()){
				number = menu.nextInt();
				}else {
					System.out.println("concentration!");
					break;
				}
			

			String instance_id = "";

			switch(number) {
			case 1: 
				listInstances();
				break;
				
			case 2: 
				availableZones();
				break;
				

			case 3:
				System.out.print("Enter instance id: ");
    		                if(id_string.hasNext())
			            instance_id = id_string.nextLine();

		                if(!instance_id.trim().isEmpty()) {
		                    System.out.print("Enter master IP or DNS name: ");
		                    String masterIp = id_string.nextLine();  // master IP를 사용자로부터 입력받기
				    startInstance(instance_id, masterIp);  // 입력받은 masterIp를 전달
                                }
                                break;

			case 4:
                                availableRegions();
                                break;

                        case 5:
                                System.out.print("Enter instance id: ");
                                if(id_string.hasNext())
                                        instance_id = id_string.nextLine();

                                if(!instance_id.trim().isEmpty()){
                                    System.out.print("Enter master IP or DNS name: ");
				    String masterIp = id_string.nextLine();
				    stopInstance(instance_id, masterIp);
				}
                                break;

			case 6:
		                System.out.print("Enter master IP or DNS name (or press Enter to create a new master): ");
		                String masterIp = id_string.nextLine();  
                                createInstance(masterIp);  
                                break;

			case 7: 
      			        System.out.print("Enter instance id: ");
                                if(id_string.hasNext())
                                        instance_id = id_string.nextLine();

                                if(!instance_id.trim().isEmpty())
                                        rebootInstance(instance_id);
                                break;


			case 8: 
				listImages();
				break;
		        case 9:
				System.out.print("Enter the host IP or DNS name: ");
				String host = id_string.nextLine();
				runCondorStatusRemote(host);
				break;


			case 99: 
				System.out.println("bye!");
				menu.close();
				id_string.close();
				return;
			default: System.out.println("concentration!");
			}

		}
		
	}

	public static void listInstances() {
		
		System.out.println("Listing instances....");
		boolean done = false;
		
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		
		while(!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);

			for(Reservation reservation : response.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
					System.out.printf(
						"[id] %s, " +
						"[AMI] %s, " +
						"[type] %s, " +
						"[state] %10s, " +
						"[monitoring state] %s",
						instance.getInstanceId(),
						instance.getImageId(),
						instance.getInstanceType(),
						instance.getState().getName(),
						instance.getMonitoring().getState());
				}
				System.out.println();
			}

			request.setNextToken(response.getNextToken());

			if(response.getNextToken() == null) {
				done = true;
			}
		}
	}




	
	public static void availableZones()	{

		System.out.println("Available zones....");
		try {
			DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
			Iterator <AvailabilityZone> iterator = availabilityZonesResult.getAvailabilityZones().iterator();
			
			AvailabilityZone zone;
			while(iterator.hasNext()) {
				zone = iterator.next();
				System.out.printf("[id] %s,  [region] %15s, [zone] %15s\n", zone.getZoneId(), zone.getRegionName(), zone.getZoneName());
			}
			System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
					" Availability Zones.");

		} catch (AmazonServiceException ase) {
				System.out.println("Caught Exception: " + ase.getMessage());
				System.out.println("Reponse Status Code: " + ase.getStatusCode());
				System.out.println("Error Code: " + ase.getErrorCode());
				System.out.println("Request ID: " + ase.getRequestId());
		}
	
	}

	public static void availableRegions() {
		
		System.out.println("Available regions ....");
		
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DescribeRegionsResult regions_response = ec2.describeRegions();

		for(Region region : regions_response.getRegions()) {
			System.out.printf(
				"[region] %15s, " +
				"[endpoint] %s\n",
				region.getRegionName(),
				region.getEndpoint());
		}
	}
	

	private static final String CONDOR_MASTER_IP = "ip-172-31-87-11.ec2.internal";
	private static final String CONDOR_MASTER_USER = "cloud";
        private static final String SSH_KEY_PATH = "/home/cloud/.ssh/cloud-test.pem";



	// Condor 풀에 노드를 추가하거나 제거하는 메서드
	    public static void updateCondorPool(String instance_id, boolean addToPool, String newMasterIp) {
	        // EC2 인스턴스의 public IP를 가져옴
	        DescribeInstancesRequest describeRequest = new DescribeInstancesRequest().withInstanceIds(instance_id);
	        DescribeInstancesResult describeResult = ec2.describeInstances(describeRequest);
	        String instanceIp = describeResult.getReservations().get(0).getInstances().get(0).getPublicIpAddress();

	        // Condor에 노드를 추가하거나 제거하는 작업
	        SSHUtil sshUtil = new SSHUtil(newMasterIp, "cloud", SSH_KEY_PATH);

	        try {
	            if (addToPool) {
	                // 워커 노드일 경우 Condor 마스터의 IP를 CONDOR_HOST로 설정
	                // 마스터 노드는 CONDOR_HOST를 설정할 필요 없음
	                if (!instanceIp.equals(newMasterIp)) {
	                    String command = "echo 'CONDOR_HOST = " + newMasterIp + "' >> /etc/condor/config.d/condor_config.local";
	                    sshUtil.executeCommand(command);
	                    System.out.printf("Added instance %s to Condor Pool as Worker Node\n", instance_id);
	                }
	            } else {
	                // Condor 마스터에서 해당 인스턴스를 제거 (instance_ip의 CONDOR_HOST 삭제)
	                String command = "sed -i '/CONDOR_HOST = " + instanceIp + "/d' /etc/condor/config.d/condor_config.local";
	                sshUtil.executeCommand(command);
	                System.out.printf("Removed instance %s from Condor Pool\n", instance_id);
	            }
	        } catch (IOException e) {
	            System.out.println("Error executing SSH command: " + e.getMessage());
	        }
	    }




	    // 인스턴스를 시작할 때 Condor Pool에 추가
	    public static void startInstance(String instance_id, String masterIp) {
	        try {
	            StartInstancesRequest startRequest = new StartInstancesRequest().withInstanceIds(instance_id);
	            ec2.startInstances(startRequest);
	            System.out.printf("Successfully started instance %s\n", instance_id);
	
	            // Condor Pool에 인스턴스 추가
	            updateCondorPool(instance_id, true, masterIp);
	        } catch (Exception e) {
	            System.out.println("Exception: " + e.toString());
	        }
	    }




	    // 인스턴스를 중지할 때 Condor Pool에서 제거
	    public static void stopInstance(String instance_id, String masterIp) {
	        try {
	            StopInstancesRequest stopRequest = new StopInstancesRequest().withInstanceIds(instance_id);
	            ec2.stopInstances(stopRequest);
	            System.out.printf("Successfully stopped instance %s\n", instance_id);
	
	            // Condor Pool에서 인스턴스 제거
	            updateCondorPool(instance_id, false, masterIp);
	        } catch (Exception e) {
        	    System.out.println("Exception: " + e.toString());
	        }
	    }







	    public static void createInstance(String masterIp) {
		    Scanner input = new Scanner(System.in);

		    // 사용자 입력 받기
		    System.out.print("Enter instance name: ");
		    String instanceName = input.nextLine();

		    System.out.print("Enter AMI ID: ");
		    String amiId = input.nextLine();

		    System.out.print("Enter instance type (e.g., t2.micro): ");
		    String instanceType = input.nextLine();

		    System.out.print("Enter key pair name: ");
		    String keyPairName = input.nextLine();

		    // userDataScript에서 master 설정 방식 결정
		    String userDataScript = "#!/bin/bash\n" +
		        "sudo su -\n" +
		        "wget https://research.cs.wisc.edu/htcondor/yum/RPM-GPG-KEY-HTCondor\n" +
		        "rpm --import RPM-GPG-KEY-HTCondor\n" +
		        "wget https://research.cs.wisc.edu/htcondor/yum/repo.d/htcondor-stable-rhel7.repo\n" +
		        "cp htcondor-stable-rhel7.repo /etc/yum.repos.d/\n" +
		        "yum install condor -y\n" +
		        "echo 'ALLOW_WRITE = *' >> /etc/condor/config.d/condor_config.local\n";

		    // masterIp가 주어진 경우 해당 인스턴스를 master로 설정
		    if (masterIp != null && !masterIp.isEmpty()) {
		        // 주어진 master IP를 사용하여 해당 인스턴스를 master로 설정
		        userDataScript += "echo 'CONDOR_HOST = " + masterIp + "' >> /etc/condor/config.d/condor_config.local\n";
		        userDataScript += "echo 'DAEMON_LIST = MASTER, STARTD' >> /etc/condor/config.d/condor_config.local\n";
		    } else {
		        // masterIp가 주어지지 않으면 새로 생성된 인스턴스를 master로 설정
		        userDataScript += "echo 'CONDOR_HOST = $(hostname -I | awk \"{print $1}\")' >> /etc/condor/config.d/condor_config.local\n"; // 새 인스턴스 IP로 설정
		        userDataScript += "echo 'DAEMON_LIST = MASTER' >> /etc/condor/config.d/condor_config.local\n"; // master만 실행
		    }

		    userDataScript += "systemctl start condor\n" +
                		      "systemctl enable condor";

		    RunInstancesRequest runRequest = new RunInstancesRequest()
		        .withImageId(amiId)
		        .withInstanceType(instanceType)
		        .withMaxCount(1)
		        .withMinCount(1)
		        .withKeyName(keyPairName)
		        .withSecurityGroups("default")
		        .withUserData(Base64.getEncoder().encodeToString(userDataScript.getBytes()));

		    RunInstancesResult runResponse = ec2.runInstances(runRequest);

		    String instanceId = runResponse.getReservation().getInstances().get(0).getInstanceId();
		    String publicIp = runResponse.getReservation().getInstances().get(0).getPublicIpAddress();

		    // 인스턴스 태그 추가
		    CreateTagsRequest createTagsRequest = new CreateTagsRequest()
		        .withResources(instanceId)
		        .withTags(new Tag().withKey("Name").withValue(instanceName));

		    ec2.createTags(createTagsRequest);

		    System.out.printf("Successfully started EC2 instance %s with name %s based on AMI %s\n", instanceId, instanceName, amiId);

		    // Condor Pool에 인스턴스 추가
		    updateCondorPool(instanceId, true, publicIp);  // 새로 생성된 인스턴스의 IP로 master 설정
		}




	static class SSHUtil {

        private String host;
        private String user;
        private String keyPath;

        public SSHUtil(String host, String user, String keyPath) {
            this.host = host;
            this.user = user;
            this.keyPath = keyPath;
        }

        // SSH 명령어 실행
        public void executeCommand(String command) throws IOException {
            // 예시로 JSch 라이브러리 사용하여 SSH 명령 실행
            // 실제로는 SSH 명령어 실행을 위해 JSch 또는 다른 SSH 라이브러리 사용
            System.out.printf("Executing command on %s: %s\n", host, command);
            // 실제 SSH 명령어 실행 코드 추가
        }
        }



	public static void rebootInstance(String instance_id) {
		
		System.out.printf("Rebooting .... %s\n", instance_id);
		
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		try {
			RebootInstancesRequest request = new RebootInstancesRequest()
					.withInstanceIds(instance_id);

				RebootInstancesResult response = ec2.rebootInstances(request);

				System.out.printf(
						"Successfully rebooted instance %s", instance_id);

		} catch(Exception e)
		{
			System.out.println("Exception: "+e.toString());
		}

		
	}
	
	public static void listImages() {
        System.out.println("Listing your images....");

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DescribeImagesRequest request = new DescribeImagesRequest();
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();

 
	List<String> ownerIds = new ArrayList<>();
        ownerIds.add("self");  

        request.setOwners(ownerIds);  
        DescribeImagesResult results = ec2.describeImages(request);

        if (results.getImages() != null && !results.getImages().isEmpty()) {
            for (Image image : results.getImages()) {
                System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s\n",
                                  image.getImageId(), image.getName(), image.getOwnerId());
            }
        } else {
            System.out.println("No images found.");
        }
    }


    static String privateKeyPath = "/home/cloud/.ssh/cloud-test.pem";
    static String user = "ec2-user";  

    public static void runCondorStatusRemote(String host) {
        String command = "condor_status"; 

        try {
            JSch jsch = new JSch();
            jsch.addIdentity(privateKeyPath); 

            Session session = jsch.getSession(user, host, 22); 
            session.setConfig("StrictHostKeyChecking", "no"); 
	    session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setErrStream(System.err);

            InputStream inputStream = channel.getInputStream();
            channel.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);  
            }

            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to execute condor_status command on " + host);
        }
    }





}

