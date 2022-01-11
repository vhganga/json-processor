
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author gangavh
 */
public class JSONFileProcessing {

    public static void main(String a[]) {

        System.out.println("JSON File Processing");
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter the file path - example /Users/abc/132.json: ");
            String filePath = bufferedReader.readLine();
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader(filePath);
            // Read JSON file
            Object obj = jsonParser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray instances = (JSONArray) jsonObject.get("Instances");
            int number_of_instances = determine_number_of_instances(instances);
            System.out.println("Number of instances: " + number_of_instances);

            java.util.List<String> list_of_environments = list_environments(instances);
            System.out.println("List of environments: ");
            for (int i = 0; i < list_of_environments.size(); i++)
                System.out.println(list_of_environments.get(i));
            System.out.println("Avg host uptime in hours: ");
            java.util.Map<String, Double> averageHostUptimes = get_average_host_uptime(instances);

            for (String ipAddress : averageHostUptimes.keySet()) {
                String key = ipAddress;
                double value = (averageHostUptimes.get(key));
                System.out.println(key + " " + value);
            }
            System.out.println("Env to IP map: ");
            Map<String, List> env_to_ip_map = map_environment_to_ips(instances);
            for (String envToIP : env_to_ip_map.keySet()) {
                String key = envToIP;
                List value = (env_to_ip_map.get(key));
                System.out.println(key + " " + value);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static int determine_number_of_instances(JSONArray instances) {
        return instances.size();
    }

    private static java.util.List list_environments(JSONArray instances) {

        java.util.List<String> uniqueEnvironments = new java.util.ArrayList();
        for (int i = 0; i < instances.size(); i++) {
            JSONObject instance = (JSONObject) instances.get(i);
            JSONObject InstanceData = (JSONObject) instance.get("InstanceData");
            String tempInstance = InstanceData.get("Environment").toString();
            if (!(uniqueEnvironments.contains(tempInstance)))
                uniqueEnvironments.add(tempInstance);
        }

        return uniqueEnvironments;
    }

    private static java.util.Map<String, Double> get_average_host_uptime(JSONArray instances) {

        String ISO_DATE_FORMAT_ZERO_OFFSET = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ISO_DATE_FORMAT_ZERO_OFFSET);
        simpleDateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(System.currentTimeMillis());

        java.util.Map<String, Double> averageHostUptimes = new java.util.HashMap();
        for (int i = 0; i < instances.size(); i++) {
            JSONObject instance = (JSONObject) instances.get(i);
            JSONObject InstanceData = (JSONObject) instance.get("InstanceData");
            String launchTime = InstanceData.get("LaunchTime").toString();
            String publicIpAddress = InstanceData.get("PublicIpAddress").toString();

            try {

                java.sql.Timestamp tempTimeStamp = new java.sql.Timestamp(simpleDateFormat.parse(launchTime).getTime());
                long difference = currentTimestamp.getTime() - tempTimeStamp.getTime();
                double days = (double) (((difference / 1000) / 3600) / 24);

                averageHostUptimes.put(publicIpAddress, days);

            } catch (Exception e) {
                System.out.println("Error while parsing timestamp");
            }

        }

        return averageHostUptimes;

    }

    private static java.util.Map<String, List> map_environment_to_ips(JSONArray instances) {
        java.util.Map<String, List> environmentToIPs = new java.util.HashMap();

        for (int i = 0; i < instances.size(); i++) {
            JSONObject instance = (JSONObject) instances.get(i);
            JSONObject InstanceData = (JSONObject) instance.get("InstanceData");
            String environment = InstanceData.get("Environment").toString();
            String publicIpAddress = InstanceData.get("PublicIpAddress").toString();

            if (environmentToIPs.containsKey(environment)) {
                List ipAddressList = environmentToIPs.get(environment);
                ipAddressList.add(publicIpAddress);
                environmentToIPs.put(environment, ipAddressList);
            } else {
                List ipAddressList = new java.util.ArrayList();
                ipAddressList.add(publicIpAddress);
                environmentToIPs.put(environment, ipAddressList);
            }

        }

        return environmentToIPs;

    }
}
