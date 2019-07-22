////////////////////////////////////////////////////////////////////////////////
///
/// @file       .jenkins.groovy
///
/// @project
///
/// @brief      Main Jenkins configuration
///
////////////////////////////////////////////////////////////////////////////////
///
////////////////////////////////////////////////////////////////////////////////
///
/// @copyright Copyright (c) 2019, Evan Lojewski
/// @cond
///
/// All rights reserved.
///
/// Redistribution and use in source and binary forms, with or without
/// modification, are permitted provided that the following conditions are met:
/// 1. Redistributions of source code must retain the above copyright notice,
/// this list of conditions and the following disclaimer.
/// 2. Redistributions in binary form must reproduce the above copyright notice,
/// this list of conditions and the following disclaimer in the documentation
/// and/or other materials provided with the distribution.
/// 3. Neither the name of the copyright holder nor the
/// names of its contributors may be used to endorse or promote products
/// derived from this software without specific prior written permission.
///
////////////////////////////////////////////////////////////////////////////////
///
/// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
/// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
/// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
/// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
/// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
/// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
/// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
/// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
/// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
/// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
/// POSSIBILITY OF SUCH DAMAGE.
/// @endcond
////////////////////////////////////////////////////////////////////////////////

def build(nodeName)
{
    node(nodeName)
    {
        stage('checkout')
        {
            checkout(
                [$class: 'GitSCM', branches: [[name: '**']],
                                browser: [$class: 'GithubWeb',
                                repoUrl: 'https://github.com/'],
                                doGenerateSubmoduleConfigurations: false,
                                extensions: [
                                    [$class: 'SubmoduleOption',
                                            disableSubmodules: false,
                                            parentCredentials: false,
                                            recursiveSubmodules: true,
                                            reference: '',
                                            trackingSubmodules: false]],
                                submoduleCfg: [],
                                userRemoteConfigs: [[url: 'https://github.com/meklort/astflash.git']]])
        }

        stage('build')
        {
            withEnv(['PATH+WHATEVER=/usr/local/bin']) {
                sh '''#!/bin/bash
                    rm -rf build
                    mkdir build
                    cd build
                    cmake .. -G Ninja
                    ninja
                '''
            }
            // archiveArtifacts 'release'
        }

        cleanWs()
    }
}


try
{
    githubNotify account: 'meklort', context: JOB_NAME, credentialsId: 'jenkins_status', description: 'Build Pending ', gitApiUrl: '', repo: 'astflash', sha: GIT_COMMIT, status: 'PENDING', targetUrl: 'http://bridge.meklort.com:8080/'
    build('master')
    build('debian')
}
catch(e)
{
    githubNotify account: 'meklort', context: JOB_NAME, credentialsId: 'jenkins_status', description: 'Build Failed ', gitApiUrl: '', repo: 'astflash', sha: GIT_COMMIT, status: 'FAILURE', targetUrl: 'http://bridge.meklort.com:8080/'
    throw e
}
finally
{
    def currentResult = currentBuild.result ?: 'SUCCESS'
    else if(currentResult == 'SUCCESS')
    {
        githubNotify account: 'meklort', context: JOB_NAME, credentialsId: 'jenkins_status', description: 'Build Passed ', gitApiUrl: '', repo: 'astflash', sha: GIT_COMMIT, status: 'SUCCESS', targetUrl: 'http://bridge.meklort.com:8080/'
    }
    else
    {
        githubNotify account: 'meklort', context: JOB_NAME, credentialsId: 'jenkins_status', description: 'Build Failed ', gitApiUrl: '', repo: 'astflash', sha: GIT_COMMIT, status: 'FAILURE', targetUrl: 'http://bridge.meklort.com:8080/'
    }
}
