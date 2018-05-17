package com.alex.session.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;

@Configuration
public class HazelcastConfig {

	@Value("${hz.name}")
	private String nodeName;
	@Value("${hz.password}")
	private String nodePassword;
	@Value("${hz.cluster}")
	private String clusterHosts;
	@Value("${hz.port}")
	private Integer nodePort;

	@Bean
	public Config config() {

		Config config = new Config();

		GroupConfig groupConfig = config.getGroupConfig();
		groupConfig.setName(nodeName);
		groupConfig.setPassword(nodePassword);

		NetworkConfig netConfig = config.getNetworkConfig();
		netConfig.setPort(nodePort).setPortAutoIncrement(false);

		JoinConfig join = netConfig.getJoin();
		join.getMulticastConfig().setEnabled(false);
		join.getAwsConfig().setEnabled(false);

		// Add cluster members
		if (StringUtils.isNotBlank(clusterHosts)) {
			for (String memberHost : StringUtils.split(clusterHosts, ',')) {
				join.getTcpIpConfig().addMember(memberHost).setEnabled(true);
			}
		}

		return config;
	}
}
