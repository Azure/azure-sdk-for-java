// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

/**
 * Fake credential shared in Tests
 */
public class FakeCredentialInTest {
    /**
     * Fake certificate content
     */
    public static final String FAKE_CERTIFICATE_CONTENT =
        "MIIJUQIBAzCCCRcGCSqGSIb3DQEHAaCCCQgEggkEMIIJADCCA7cGCSqGSIb3DQEH"
            + "BqCCA6gwggOkAgEAMIIDnQYJKoZIhvcNAQcBMBwGCiqGSIb3DQEMAQYwDgQIONsr"
            + "wr1FhuICAggAgIIDcBQWaArZgVr5K8+zccadOCGIC+WzlAx2V88HT6fQcujZItr3"
            + "koiHi7+sdTe3mQncnxqZoNGgx4s6Xh+QIIFdHSAo4EL9uGoFKiprKMHAmiCj9Pcm"
            + "M6stTFYMnzmlAiVLCNogPEobi2pfcQIVbDaVUHdm4EczlBGKyMTZSTkXmxI7Ax9V"
            + "YXucniBpxJ3d0bTHchvHCjqHCLTDCnqyPXTqQH0JpHSYdcq9pxydtoNNgT7NDrKM"
            + "0QtxIvI29+ZlSLZMxB3Mf4qOhn6bmyBakUypK1S8N0b2YPLTjp5+Zmb8W24+1bVm"
            + "gV2p10SFjbt8CacDl3dmRUkwu6C8Cl3QIpgwbMoP8hnJppyaFvIqar9roNNS3seG"
            + "8RDn/Q4DCYWJ6JhA6Z+gDL3BncwE2q9rekkOwo1MwERNhBEtINrXztKogdA/as1o"
            + "O443ZbM/qm5pX9ZPh4Hv8Hzgl0aqlxubsUcEr8SIDNEJ3u91/gDdmHWgabnLZif8"
            + "A7e2TMCqTiCM2nRr3soNUvOqnLHoexAKqsQAvi36VVmdAH1085Q+ISpVseCe3Piq"
            + "PhLsqyfF2Yox+NkI/nOwtg0XhO+mdoAkes03ojctqFXB2ygo/iRH2ng16zGnWes0"
            + "nSp3rcOaoqcE1c85+/QQZZrzVspdnnNhYWWr1IiwyiOctOAovpmU9oDacY+1u9dO"
            + "pnVRr5ibwR1NSlIVl1KPNsYmQoP9hig8lULeVGLQTWEQc8qb55t/Y/RpgNFEs3pi"
            + "Hmd12R9NZMBcrZp3bbSzdS51OicQ6PKRXKESHVMbbsLiR8M62Dxg9ysH0kVEdxjw"
            + "LfdlqAPby/+/L2t62WKkoHq37GtqtVDYAELBsP9tq3AF+ucUB1Gj8vvwEAedJ2Zl"
            + "Q2f9xVTHXr0Ah3JkYsMpAuK0HZzMTVc0ZKXrfocbtvwr4aVwc3zOP+pz1AhqZpkD"
            + "fr23NVkAmV63aIBOr1TSNPCnn7PMlr4rfZ2vzwBKCrfnc+O44IsWNg1N4ZBAKjnh"
            + "ZZjhgxRYC5en7PKVPHAla2R8299RJy7tuiR6qo58UZNdsIJXBbjhytLroZHvdF3r"
            + "mSTxgYli5h9xKAw9c6eqmrmGNRD1dY9bmkgFNwF6C8Yi4RdCZ3C6LNFHhgxMwbXi"
            + "Xl5Mfa7E4ZSOWIeH8I79knxDPDMm4sTRSncbyn8wggVBBgkqhkiG9w0BBwGgggUy"
            + "BIIFLjCCBSowggUmBgsqhkiG9w0BDAoBAqCCBO4wggTqMBwGCiqGSIb3DQEMAQMw"
            + "DgQI4fPTwJwGln0CAggABIIEyE1waejpdCUGBbzwCZhdul9adyBO8futpEZKKlcc"
            + "RnP2iQ82N7P2CE7cXEch8OrC3+qyvyGfqVzNpjOWT1v+uMrqT67enK00/eU/OWxk"
            + "2edizJXUr+usLjojPh1Yu822Ffax3qiZ87Svm9staSNebek6q/2W24KnaDNvqPPT"
            + "vGA4prwpwdn98NHGQou5WQiSsh+VkT49duZxO6/+TWK8I27FnoyCgiKEjr6vvY6a"
            + "x4E3ect4Kz0MZsLKNYd6/BqBRw2UnrKg0yoIYHvP/j/DT8q++cafs9ZSS2eO4ZlC"
            + "5DAshQdXUD6O7fJF+rI5Ao36keWlkz8DKi0kWL32Rzvk56vVbVGIkrGveZ19E5WR"
            + "3kqkFNddO+zZs6tJJeO8Rghylp43mgyivpkzPQ6By9aekn+VgQ5Oqze7gUX74CD0"
            + "onjf5Q5eaOl6ZGdcVlKOXbf/r8libAq7GvGICm1Rfa79/Q1IqvvKFmxd/WZfa1iJ"
            + "OwZwaV53ALhlDejdTU1YS7ZHorFTJGfn4LtHoVkRpZsMnA+ygMZ0+vTTgnGS1GZz"
            + "g7OACuXWla1Dh2yv/UYKpdnhgyAGgCcIIguiRSD/JWxZxiT9sb/t+bN7NLRYpXak"
            + "rYTOi1lHoqCGfZTlzMyZPmo/DfZTdhGXVUYA6puvi+Qv22ub9N01riv2TN9noOkB"
            + "RH67I48dXRrzJi7m2CYG6v8pQmvW4Tg3feIrOF99hHU/YJfOWvQgjiQoyJFlyq8L"
            + "1wwhG4eXQH4bP97ilJHFDWjTzKzbYrhKZadd7SJ2hT6R3NPH9AYyMdsoPaWu9RIE"
            + "g2niz0niFXwUnNQib/deL7PDyFwndsRtp3P405oF4tzMU1Q4mD2IwObM7g4+syFW"
            + "c+2Cy29o0buJrb4jIsIjjUYNB/mzoU7iKXwQ0qhPTHyUbP4XM5jaiEuS48u4hRbh"
            + "k9C5Ti6fvrbeVqN/rcXPvS0h+HCf4Gc8LCXTBME0a1SSnQR10q66GRnuQa2hM+/b"
            + "AxQUTXNYs/p4Np8aGIR6EkXXR0cbcoMHp3+d6h9B8tqlmvTYAFYvlkImeyJaNOpH"
            + "J9E+AbNEugEm1s+GgfQT5XKCThmpg0uNyKFAkjvkXjoS5K4dJwQPtYfM2SYyLjTO"
            + "dEmsjPKR7NcBIR3hx35PIpyHxdqAnb25GakB7GHX1/HJsZCf+NLuUsWkyP6pNy6w"
            + "o9l9BOSSDnUPEV5D/J1h/GZ/hOHcf9WDv06KefKAy77UpnTKSSlHr/PzkfNbtjFf"
            + "6xKPQRWA1WVd3FW2BETdABteX0QcYSZjVRjirWZUOxu2VKv9I4G0PGMjmo6UxCMG"
            + "xFV1qulKn+kPAInoNbgbY2ZaF5q1FAoMQ4nAPG6W79J0xgEkuCiH6F7F7TEHldCO"
            + "ulHWfJja7K27zW2T4ZnQbcpKmHpCns7bAt0198CrYyHfNP4Yyx0uiXBI+Z9hlHvO"
            + "kcs0l5RDV1EWR3jOih7zLr43MPwJ12sXwEMCOjUHYxs0jTZcgmti+wBPs8xuWayh"
            + "J/9pD1DfFxf6lFOCi1op5zPc7U3NNMbU3gXgSolsrMjm0dJH0rfu4+C0cym62EBo"
            + "IGdvyABqS9N96YUu1OreBcCYiTP5Qajn87J8i9zj3aa5lFGJYCS6s8EBeDElMCMG"
            + "CSqGSIb3DQEJFTEWBBTbFJe8LIKzZcXHxz9hFRPuEXeQqTAxMCEwCQYFKw4DAhoF"
            + "AAQUI7HzgLxeU0ExCw7mUkJyWmnUlckECNF1gKFeLQMGAgIIAA==";
}
